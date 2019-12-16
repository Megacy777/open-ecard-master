/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.scio;

import static org.mockito.Mockito.*;
import org.testng.annotations.Test;

/**
 *
 * @author Neil Crossley
 */
public class NFCCardMonitoringTest {

    private static final long TIMEOUT_MILLISECONDS = 3000;

    @Test(timeOut = TIMEOUT_MILLISECONDS)
    public void sutShouldNotWaitIfStoppedBeforeRunning() {
	NFCCardTerminal terminal = createTerminal();

	NFCCardMonitoring sut = createSut(terminal);

	sut.notifyStopMonitoring();
	sut.run();
    }

    @Test(timeOut = TIMEOUT_MILLISECONDS)
    public void sutShouldRemoveCardWhenTagIsMissing() {
	AbstractNFCCard nfcCard = createNfcCard();
	when(nfcCard.isTagPresent()).thenReturn(Boolean.FALSE);
	NFCCardTerminal terminal = createTerminal();

	NFCCardMonitoring sut = createSut(terminal, nfcCard);

	sut.run();

	verify(terminal).removeTag();
    }

    public static NFCCardMonitoring createSut() {
	return createSut(createTerminal(), createNfcCard());
    }

    public static NFCCardMonitoring createSut(NFCCardTerminal terminal, AbstractNFCCard nfcCard) {
	return new NFCCardMonitoring(terminal, nfcCard);
    }

    public static NFCCardMonitoring createSut(NFCCardTerminal terminal) {
	return createSut(terminal, createNfcCard());
    }

    public static NFCCardTerminal createTerminal() {
	return mock(NFCCardTerminal.class);
    }

    public static AbstractNFCCard createNfcCard() {
	return mock(AbstractNFCCard.class);
    }
}
