package org.openecard.ws;

import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was generated by Apache CXF 3.3.2
 * Generated source version: 3.3.2
 *
 */
@XmlSeeAlso({org.w3._2007._05.xmldsig_more_.ObjectFactory.class, org.w3._2001._04.xmlenc_.ObjectFactory.class, de.bund.bsi.ecard.api._1.ObjectFactory.class, org.etsi.uri._01903.v1_3.ObjectFactory.class, oasis.names.tc.dss_x._1_0.profiles.verificationreport.schema_.ObjectFactory.class, org.openecard.ws.schema.ObjectFactory.class, org.openecard.ws.chipgateway.ObjectFactory.class, org.w3._2001._04.xmldsig_more_.ObjectFactory.class, oasis.names.tc.dss._1_0.core.schema.ObjectFactory.class, org.w3._2000._09.xmldsig_.ObjectFactory.class, generated.ObjectFactory.class, iso.std.iso_iec._24727.tech.schema.ObjectFactory.class, org.w3._2009.xmlenc11_.ObjectFactory.class, oasis.names.tc.saml._2_0.assertion.ObjectFactory.class, org.etsi.uri._02231.v3_1.ObjectFactory.class, oasis.names.tc.saml._1_0.assertion.ObjectFactory.class})
public interface IFDCallback {

    @ECardApiMethod(operationName = "SignalEvent", action = "urn:iso:std:iso-iec:24727:tech:schema:SignalEvent")
    public iso.std.iso_iec._24727.tech.schema.SignalEventResponse signalEvent(
        iso.std.iso_iec._24727.tech.schema.SignalEvent parameters
    );
}
