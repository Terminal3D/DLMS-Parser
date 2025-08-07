package com.example.dlms_parser

import com.example.dlms_parser.utils.XmlToJsonConverter

fun main() {
    val originalXml = """<ActionRequest>
  <ActionRequestNormal>
    <InvokeIdAndPriority Value="42" />
    <ActionRequest>
      <MethodDescriptor>
        <ClassId Value="0012" />
        <InstanceId Value="00002C0000FF" />
        <MethodId Value="01" />
      </MethodDescriptor>
      <MethodInvocationParameters>
        <Structure Qty="02" >
          <OctetString Value="4B464D414432314E5F45564E5F42475F324B5F384D5F44535F7639303039" />
          <UInt32 Value="0005A0B2" />
        </Structure>
      </MethodInvocationParameters>
    </ActionRequest>
  </ActionRequestNormal>
</ActionRequest>
"""
    
    println("Original XML:")
    println(originalXml)
    println()
    
    println("JSON conversion result:")
    val jsonResult = XmlToJsonConverter.convertXmlToJson(originalXml)
    println(jsonResult)
}