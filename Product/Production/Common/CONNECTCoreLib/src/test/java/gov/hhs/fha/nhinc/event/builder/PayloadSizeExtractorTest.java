/*
 * Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above
 *       copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the United States Government nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.hhs.fha.nhinc.event.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import oasis.names.tc.ebxml_regrep.xsd.rim._3.ExtrinsicObjectType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.SlotType1;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ValueListType;

import org.junit.Test;

import com.google.common.base.Optional;

/**
 * @author akong
 * 
 */
public class PayloadSizeExtractorTest {

    @Test
    public void extract() {
        ExtrinsicObjectType extrinsicObject = createExtrinsicObject("size", "value");       
        JAXBElement<ExtrinsicObjectType> jaxbWrapper = wrapExtrinsicObject(extrinsicObject);

        PayloadSizeExtractor extractor = new PayloadSizeExtractor();
        Optional<String> payloadSize = extractor.apply(jaxbWrapper);
        
        assertEquals("value", payloadSize.get());
    }
    
    @Test
    public void absent() {
        ExtrinsicObjectType extrinsicObject = createExtrinsicObject("test", "value");       
        JAXBElement<ExtrinsicObjectType> jaxbWrapper = wrapExtrinsicObject(extrinsicObject);

        PayloadSizeExtractor extractor = new PayloadSizeExtractor();
        Optional<String> payloadSize = extractor.apply(jaxbWrapper);
        
        assertFalse(payloadSize.isPresent());
    }

    private JAXBElement<ExtrinsicObjectType> wrapExtrinsicObject(ExtrinsicObjectType extrinsicObject) {
        QName qName = mock(QName.class);
        return new JAXBElement<ExtrinsicObjectType>(qName, ExtrinsicObjectType.class, extrinsicObject);
    }

    private ExtrinsicObjectType createExtrinsicObject(String slotName, String slotValue) {
        ExtrinsicObjectType extrinsicObject = new ExtrinsicObjectType();

        SlotType1 slot = new SlotType1();
        slot.setName(slotName);

        ValueListType valueList = new ValueListType();
        valueList.getValue().add(slotValue);

        slot.setValueList(valueList);
        extrinsicObject.getSlot().add(slot);

        return extrinsicObject;
    }

}
