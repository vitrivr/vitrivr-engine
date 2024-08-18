package org.vitrivr.engine.query.operators.input

import libsvm.svm
import libsvm.svm_node
import libsvm.svm_parameter
import libsvm.svm_problem
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.query.model.api.input.InputData
import org.vitrivr.engine.query.model.api.input.VectorInputData
import org.vitrivr.engine.query.model.input.InputDataTransformer
import org.vitrivr.engine.query.model.input.InputDataTransformerFactory

class SVMVectorTransformerFactory : InputDataTransformerFactory {
    override fun newTransformer(
        name: String,
        inputs: List<InputData>,
        schema: Schema,
        context: Context
    ): InputDataTransformer {

        val vectors = inputs.filterIsInstance<VectorInputData>().map { it.data }

        var positiveCount = context[name, "positives"]?.toIntOrNull()
        var negativeCount = context[name, "negatives"]?.toIntOrNull()

        if (positiveCount == null && negativeCount == null && vectors.size % 2 == 1) {
            throw IllegalArgumentException("Cannot identify split between positive and negative samples. Provide property 'positives' or 'negatives' to disambiguate")
        }

        if (positiveCount != null) {
            if (positiveCount <= 0) {
                throw IllegalArgumentException("'positiveCount' needs to be positive")
            }
            if (positiveCount > vectors.size) {
                throw IllegalArgumentException("'positiveCount' larger than number of provided inputs")
            }
            if (negativeCount == null) {
                negativeCount = vectors.size - positiveCount
            }
        }

        if (negativeCount != null) {
            if (negativeCount <= 0) {
                throw IllegalArgumentException("'negativeCount' needs to be positive")
            }
            if (negativeCount > vectors.size) {
                throw IllegalArgumentException("'negativeCount' larger than number of provided inputs")
            }
            if (positiveCount == null) {
                positiveCount = vectors.size - negativeCount
            }
        }

        if (positiveCount!! + negativeCount!! != vectors.size) {
            throw IllegalArgumentException("'positiveCount' and 'negativeCount' do not sum to number of provided inputs")
        }

        return SVMVectorTransformer(
            vectors.subList(0, positiveCount),
            vectors.subList(positiveCount, vectors.size)
        )

    }

    class SVMVectorTransformer(private val positive : Collection<List<Float>>, private val negative: Collection<List<Float>>) : InputDataTransformer {

        override fun transform(): InputData {

            val pos = positive.map { toSVMNode(it) }
            val neg = negative.map { toSVMNode(it) }

            val x = (pos + neg).toTypedArray()
            val y = DoubleArray(positive.size) { 1.0 } + DoubleArray(negative.size) { -1.0 }

            val prob = svm_problem().apply {
                this.l = x.size
                this.y = y
                this.x = x
            }

            // Set up the SVM parameters
            val param = svm_parameter().apply {
                svm_type = svm_parameter.C_SVC
                kernel_type = svm_parameter.LINEAR
                degree = pos[0].size - 1
                gamma = 0.0
                coef0 = 0.0
                nu = 0.5
                cache_size = 100.0
                C = 1.0
                eps = 1e-3
                p = 0.1
                shrinking = 0
                probability = 1
                nr_weight = 0
            }

            // Train the SVM model
            val model = svm.svm_train(prob, param)

            // Extract the hyperplane (normal vector of the hyperplane)

            val supportVectors = model.SV
            val coefs = model.sv_coef[0]

            val w = FloatArray(pos[0].size - 1){ 0f }

            for (idx in supportVectors.indices) {
                val support = supportVectors[idx]
                val coef = coefs[idx].toFloat()
                for (i in support.indices) {
                    val s = support[i]
                    if (s.index > 0) {
                        w[s.index - 1] += s.value.toFloat() * coef
                    }
                }
            }

            if (model.label[0] < 0) {
                for (i in w.indices) {
                    w[i] *= -1f
                }
            }

            return VectorInputData(w.toList())

        }

        private fun toSVMNode(vector: List<Float>): Array<svm_node> {
            val list = vector.mapIndexed { index, d ->
                svm_node().also { node ->
                    node.index = index + 1
                    node.value = d.toDouble()
                }
            }.toMutableList()
            list.add(svm_node().also { it.index = -1 })
            return list.toTypedArray()
        }
    }

}