package io.zeebe.zeeqs.data.service

data class BpmnExtensionProperty(
        val name: String?,
        val value: String?
)

data class IoMapping (
        val source: String?,
        val target: String?
)
data class ExtensionIoMapping(
        val inputs: List<IoMapping>? = emptyList(),
        val outputs: List<IoMapping>? = emptyList(),
)
data class BpmnExtensionElements(
        val properties: List<BpmnExtensionProperty>? = emptyList(),
        val ioMapping: ExtensionIoMapping? = ExtensionIoMapping(),
)

