package com.memorae.prototype.map

enum class BackdropKernel(
    val sampleCount: Int,
    internal val shaderMode: Float,
) {
    Raw(sampleCount = 1, shaderMode = 0f),
    FiveSample(sampleCount = 5, shaderMode = 1f),
    NineSample(sampleCount = 9, shaderMode = 2f),
    ThirteenSample(sampleCount = 13, shaderMode = 3f),
}

data class BackdropSofteningSpec(
    val sampleRadiusPx: Float,
    val sampleStrength: Float,
    val kernel: BackdropKernel,
) {
    companion object {
        val Raw = BackdropSofteningSpec(
            sampleRadiusPx = 0f,
            sampleStrength = 0f,
            kernel = BackdropKernel.Raw,
        )

        val Soft = BackdropSofteningSpec(
            sampleRadiusPx = 1.75f,
            sampleStrength = 0.46f,
            kernel = BackdropKernel.FiveSample,
        )

        val Medium = BackdropSofteningSpec(
            sampleRadiusPx = 2.75f,
            sampleStrength = 0.64f,
            kernel = BackdropKernel.NineSample,
        )

        val Strong = BackdropSofteningSpec(
            sampleRadiusPx = 4.25f,
            sampleStrength = 0.82f,
            kernel = BackdropKernel.ThirteenSample,
        )
    }
}
