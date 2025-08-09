package com.example.dlms_parser.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed class DlmsMessage {
    abstract val rawData: String
    abstract val type: String
    abstract val xmlStructure: String?
    abstract val originalXml: String?
}

@Serializable
data class AarqMessage(
    override val rawData: String,
    override val type: String = "AARQ",
    override val xmlStructure: String? = null,
    override val originalXml: String? = null,
    val applicationContextName: String,
    val callingApTitle: String? = null,
    val senderAcseRequirements: Boolean,
    val mechanismName: String,
    val callingAuthenticationValue: String? = null,
    val userInformation: String,
    val initiateRequest: InitiateRequest
) : DlmsMessage()

@Serializable
data class AareMessage(
    override val rawData: String,
    override val type: String = "AARE",
    override val xmlStructure: String? = null,
    override val originalXml: String? = null,
    val applicationContextName: String,
    val associationResult: AssociationResult,
    val resultSourceDiagnostic: String?,
    val userInformation: String,
    val initiateResponse: InitiateResponse
) : DlmsMessage()

@Serializable
data class GetRequestMessage(
    override val rawData: String,
    override val type: String = "GetRequest",
    override val xmlStructure: String? = null,
    override val originalXml: String? = null,
    val requestType: RequestType,
    val invokeId: Int,
    val attribute: String,
    val accessSelector: String? = null
) : DlmsMessage()

@Serializable
data class GetResponseMessage(
    override val rawData: String,
    override val type: String = "GetResponse",
    override val xmlStructure: String? = null,
    override val originalXml: String? = null,
    val responseType: ResponseType,
    val invokeId: Int,
    val dataType: String,
    val data: String
) : DlmsMessage()

@Serializable
data class ActionRequestMessage(
    override val rawData: String,
    override val type: String = "ActionRequest",
    override val xmlStructure: String? = null,
    override val originalXml: String? = null,
    val requestType: RequestType,
    val invokeId: Int,
    val method: String,
    val parameters: ActionParameters
) : DlmsMessage()

@Serializable
data class ActionResponseMessage(
    override val rawData: String,
    override val type: String = "ActionResponse",
    override val xmlStructure: String? = null,
    override val originalXml: String? = null,
    val responseType: ResponseType,
    val invokeId: Int,
    val actionResult: ActionResult
) : DlmsMessage()

@Serializable
data class SetRequestMessage(
    override val rawData: String,
    override val type: String = "SetRequest",
    override val xmlStructure: String? = null,
    override val originalXml: String? = null
) : DlmsMessage()

@Serializable
data class SetResponseMessage(
    override val rawData: String,
    override val type: String = "SetResponse",
    override val xmlStructure: String? = null,
    override val originalXml: String? = null
) : DlmsMessage()

@Serializable
data class ReadRequestMessage(
    override val rawData: String,
    override val type: String = "ReadRequest",
    override val xmlStructure: String? = null,
    override val originalXml: String? = null
) : DlmsMessage()

@Serializable
data class ReadResponseMessage(
    override val rawData: String,
    override val type: String = "ReadResponse",
    override val xmlStructure: String? = null,
    override val originalXml: String? = null
) : DlmsMessage()

@Serializable
data class WriteRequestMessage(
    override val rawData: String,
    override val type: String = "WriteRequest",
    override val xmlStructure: String? = null,
    override val originalXml: String? = null
) : DlmsMessage()

@Serializable
data class WriteResponseMessage(
    override val rawData: String,
    override val type: String = "WriteResponse",
    override val xmlStructure: String? = null,
    override val originalXml: String? = null
) : DlmsMessage()

@Serializable
data class InitiateRequest(
    val responseAllowed: Boolean,
    val proposedDlmsVersionNumber: Int,
    val proposedConformance: List<String>,
    val clientMaxReceivePduSize: Int
)

@Serializable
data class InitiateResponse(
    val negotiatedDlmsVersionNumber: Int,
    val negotiatedConformance: List<String>,
    val serverMaxReceivePduSize: Int,
    val vaaName: Int
)

@Serializable
data class ActionParameters(
    val structure: List<ActionParameter>
)

@Serializable
sealed class ActionParameter {
    @Serializable
    data class OctetString(val value: String) : ActionParameter()
    
    @Serializable
    data class DoubleLongUnsigned(val value: Long) : ActionParameter()
}

@Serializable
enum class AssociationResult {
    ACCEPTED,
    REJECTED_PERMANENT,
    REJECTED_TRANSIENT,
    UNKNOWN
}

@Serializable
enum class RequestType {
    NORMAL,
    NEXT,
    WITH_LIST
}

@Serializable
enum class ResponseType {
    NORMAL,
    WITH_DATABLOCK,
    WITH_LIST
}

@Serializable
enum class ActionResult {
    SUCCESS,
    HARDWARE_FAULT,
    TEMPORARY_FAILURE,
    READ_WRITE_DENIED,
    OBJECT_UNDEFINED,
    OBJECT_CLASS_INCONSISTENT,
    OBJECT_UNAVAILABLE,
    TYPE_UNMATCHED,
    SCOPE_OF_ACCESS_VIOLATED,
    DATA_BLOCK_UNAVAILABLE,
    LONG_ACTION_ABORTED,
    NO_LONG_ACTION_IN_PROGRESS,
    OTHER_REASON
}