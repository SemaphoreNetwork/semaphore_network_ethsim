package com.semaphore.usim;

import javacard.framework.*;

public class EthSIM {
	// initial menu string
	private byte[] STK_Header = { (byte) 'S', (byte) 'T', (byte) 'K', (byte) ' ', (byte) 'S', (byte) 'e', (byte) 'r',
			(byte) 'v', (byte) 'i', (byte) 'c', (byte) 'e', (byte) 's', };

	private byte[] Title = {
			// "EthSIM Wallet"
			(byte) 'E', (byte) 't', (byte) 'h', (byte) 'S', (byte) 'I', (byte) 'M', (byte) ' ', (byte) 'W', (byte) 'a',
			(byte) 'l', (byte) 'l', (byte) 'e', (byte) 't', };

	// Strings for Wallet Flows
	// Send ETH
	private byte[] SendEth = { (byte) 'S', (byte) 'e', (byte) 'n', (byte) 'd', (byte) ' ', (byte) 'E', (byte) 't',
			(byte) 'h', (byte) ':' };

	// GET INPUT TEXT
	private byte[] AmountToSend = { (byte) 'A', (byte) 'm', (byte) 'o', (byte) 'u', (byte) 'n', (byte) 't', (byte) ' ',
			(byte) 't', (byte) 'o', (byte) ' ', (byte) 'S', (byte) 'e', (byte) 'n', (byte) 'd', (byte) ':' };

	private byte[] AmountDetails = { (byte) 'i', (byte) 'n', (byte) 'c', (byte) 'l', (byte) 'u', (byte) 'd', (byte) 'e',
			(byte) ' ', (byte) 'd', (byte) 'e', (byte) 'c', (byte) 'i', (byte) 'm', (byte) 'a', (byte) 'l', (byte) ' ',
			(byte) 'u', (byte) 'p', (byte) ' ', (byte) 't', (byte) 'o', (byte) ' ', (byte) '1', (byte) '8', (byte) ' ',
			(byte) 'p', (byte) 'l', (byte) 'a', (byte) 'c', (byte) 'e', (byte) 's' };

	private byte[] AddressToSendTo = { (byte) 'A', (byte) 'd', (byte) 'd', (byte) 'r', (byte) 'e', (byte) 's',
			(byte) 's', (byte) ' ', (byte) 't', (byte) 'o', (byte) ' ', (byte) 'S', (byte) 'e', (byte) 'n', (byte) 'd',
			(byte) ' ', (byte) 't', (byte) 'o', (byte) ':', };

	private byte[] Sign = { (byte) 'S', (byte) 'i', (byte) 'g', (byte) 'n' };

	private byte[] EnterPin = { (byte) 'E', (byte) 'n', (byte) 't', (byte) 'e', (byte) 'r', (byte) ' ', (byte) 'P',
			(byte) 'I', (byte) 'N' };

	private byte[] CopySig = { (byte) 'C', (byte) 'o', (byte) 'p', (byte) 'y', (byte) ' ', (byte) 'S', (byte) 'i',
			(byte) 'g' };

	private byte[] BlindSign = { (byte) 'B', (byte) 'l', (byte) 'i', (byte) 'n', (byte) 'd', (byte) ' ', (byte) 'S',
			(byte) 'i', (byte) 'g', (byte) 'n' };

	private byte[] NoSig = { (byte) 'N', (byte) 'o', (byte) ' ', (byte) 'S', (byte) 'i', (byte) 'g' };

	private byte[] Success = { (byte) 'S', (byte) 'u', (byte) 'c', (byte) 'c', (byte) 'e', (byte) 's', (byte) 's' };

	private byte[] Fail = { (byte) 'F', (byte) 'a', (byte) 'i', (byte) 'l' };

	// buffers to save the values entered by user
	private byte[] amountBuffer;
	private byte[] addressBuffer;
	private byte[] pinBuffer;

	// hash and signature
	private byte[] hashBuffer;
	private byte[] sigBuffer;

	// Item Identifiers:
	private final byte AMOUNT_TO_SEND = (byte) 0x00;
	private final byte ADDRESS_TO_SEND_TO = (byte) 0x01;
	private final byte ENTER_PIN = (byte) 0x02;
	private final byte BLIND_SIGN = (byte) 0x03;
	private final byte COPY_SIG = (byte) 0x04;

	// Most of the Lengths of these
	public short AmtToSendLe = (short) 43;
	public short AddToSendToLe = (short) 37;
	public short EnterPinLe = (short) 27;
	public short CopySigLe = (short) 26;
	public short BlindSignLe = (short) 28;

	// Mask defining the SIM Toolkit features required by the Applet
	// It is a bitmask with 1s reflecting the needed profiles.
	private byte[] terminalProfileMask = { (byte) 0x09, (byte) 0x03, (byte) 0x21, (byte) 0x70, (byte) 0x0D };
	// Volatile RAM temporary buffer for storing intermediate data and results
	// It is 180 bytes, enough for a long SMS + dialing number.
	//
	private byte[] tempBuffer;

	// STK Commands
	private final byte SETUP_MENU = (byte) 0x25;
	private final byte SELECT_ITEM = (byte) 0x24;
	private final byte GET_INPUT = (byte) 0x23;
	private final byte DISPLAY_TEXT = (byte) 0x21;

	// default command details
	private static byte CMD_NUMBER = (byte) 0x01;

	// selection 0 is setup menu
	public byte selection = (byte) 0;

	//
	public byte getInputSelection = (byte) 0;

	// current Byte being processed
	private short offset;

	public boolean stkSelected = false;

	public byte[] tempBuf;

	// input bufs
	public byte[] inputAmt;

	public byte[] inputAddress;

	public byte[] inputPin;

	// next fetch response buf
	public byte[] nextFetchBuf;
	//
	public byte nextFetchLe;

	public boolean initialFetch = true;

	public byte nextToFetch;

	// CONSTS
	// menuLength
	private byte envelopeLe = (byte) 93;

	// *********ETH Wallet Class Objects***************//
	public ETHWallet wallet;
	// inputHash points to the hash input from the "Blind Sign" STK applet input
	public byte[] inputHash;
	// outputSig points to the sig that is output from the eth wallet class.
//	public byte[] outputSig;

	public static byte DERFormatLe = (byte) 74;

	private Sha3 m_sha3 = null;

	public EthSIM() {
		tempBuffer = JCSystem.makeTransientByteArray((short) 64, JCSystem.CLEAR_ON_RESET);
		amountBuffer = JCSystem.makeTransientByteArray((short) 24, JCSystem.CLEAR_ON_RESET);
//		outputSig = JCSystem.makeTransientByteArray((short) 65, JCSystem.CLEAR_ON_RESET);

		addressBuffer = JCSystem.makeTransientByteArray((short) 40, JCSystem.CLEAR_ON_RESET);
		pinBuffer = JCSystem.makeTransientByteArray((short) 10, JCSystem.CLEAR_ON_RESET);
//		this.initialFetch = true;

		this.wallet = new ETHWallet(inputHash);
//		m_sha3 = new Sha3();

		JCSystem.beginTransaction();
		byte[] old = this.nextFetchBuf;

		if (old != null) {
			JCSystem.requestObjectDeletion();
		}
		JCSystem.commitTransaction();

	}

	public void handleMenuSelectionEnvelope(APDU apdu) {
		// todo: some data needs to be returned here.
		// ignore making into buf.
		// should be (short)0x9126
		short sw = Util.makeShort((byte) 0x91, (byte) 0x5a);
		// the selection is the main menu, fetch accordingly

		this.stkSelected = true;
		// return 91+Le resposnse
		ISOException.throwIt(sw);

	}

	public void crateCommandDetails(byte[] cmdBuf, byte cmdType, byte cmdQualifier) {

		cmdBuf[this.offset] = CMD_NUMBER;
		this.offset++;

		cmdBuf[this.offset] = cmdType;
		this.offset++;

		cmdBuf[this.offset] = cmdQualifier;
		this.offset++;

	}

	// todo; switch for devid to 8281
	public void createDevId(byte[] cmdBuf) {
		this.offset++;

		cmdBuf[this.offset] = (byte) 0x81;
		this.offset++;

		cmdBuf[this.offset] = (byte) 0x82;
		this.offset++;
	}

	public void createInitialTLV(byte[] retBuf) {
		this.offset++;
		// TLV 81 is byte 6
		retBuf[this.offset] = (byte) 0x81;
		this.offset++;
		// Le of CMD is byte 7 should == 3
		retBuf[this.offset] = (byte) 0x03;
		this.offset++;

	}

	// response to initial fetch
	public void handleMenuSetup(byte[] retBuf, byte retLen) {

		this.offset = 0;
		this.tempBuf[this.offset] = (byte) 0xd0;
		this.offset++;
		this.tempBuf[this.offset] = retLen;

		this.createInitialTLV(retBuf);

		this.crateCommandDetails(retBuf, SETUP_MENU, (byte) 0x00);
		// next byte should be TLV 82
		retBuf[this.offset] = (byte) 0x82;

		// next byte should be Le of the Dev Id
		this.offset++;
		retBuf[this.offset] = (byte) 0x02;
		this.createDevId(retBuf);
//		// next byte TLV is 85
		retBuf[this.offset] = (byte) 0x85;
//		// next byte is Le of the alpha identifier.
		this.offset++;

		retBuf[this.offset] = (byte) STK_Header.length;
		this.offset++;
//
//		// may need normal arryCopy
//		// copy the STK Header into the buf

		Util.arrayCopyNonAtomic(STK_Header, (short) 0, retBuf, (short) this.offset, (short) STK_Header.length);
//		// off by one here?
		this.offset += STK_Header.length;
//		this.offset++;
//		// TLV of 8f
//		this.offset++;
		retBuf[this.offset] = (byte) 0x8f;
//		// Le of ItemID + item string ie. Title string length + 1
		this.offset++;
		retBuf[this.offset] = (byte) (Title.length + 1);
//		// item identifier
		this.offset++;
		retBuf[this.offset] = (byte) 0x80;
//
//		// copy title into tlv
		this.offset++;
		Util.arrayCopyNonAtomic(Title, (short) 0, retBuf, (short) this.offset, (short) Title.length);
		this.offset += Title.length;

		retBuf[this.offset] = (byte) 0x18;
		this.offset++;

		retBuf[this.offset] = (byte) 0x01;
		this.offset++;

		retBuf[this.offset] = (byte) 0x10;

		this.offset = 0;
		return;
	}

	public static byte createInitialTLVO(byte[] retBuf, byte offset) {
		// TLV 81 is byte 6
		retBuf[offset] = (byte) 0x81;
		offset++;
		// Le of CMD is byte 7 should == 3
		retBuf[offset] = (byte) 0x03;

		return offset;

	}

	public static byte createCommandDetailsO(byte[] cmdBuf, byte cmdType, byte cmdQualifier, byte offset) {

		cmdBuf[offset] = CMD_NUMBER;
		offset++;

		cmdBuf[offset] = cmdType;
		offset++;

		cmdBuf[offset] = cmdQualifier;

		return offset;

	}

	// todo; switch for devid to 8281
	public static byte createDevIdO(byte[] cmdBuf, byte offset) {

		// tag
		cmdBuf[offset] = (byte) 0x82;
		offset++;
		// le
		cmdBuf[offset] = (byte) 0x02;
		offset++;

		cmdBuf[offset] = (byte) 0x81;
		offset++;

		cmdBuf[offset] = (byte) 0x82;
		return offset;
	}

	public void handleRootFetch(byte[] retBuf, byte retLen) {

		byte offset = 0;

		this.tempBuf[offset] = (byte) 0xd0;
		offset++;

		this.tempBuf[offset] = retLen;
		offset++;

		offset = createInitialTLVO(retBuf, offset);
		offset++;

		offset = createCommandDetailsO(retBuf, SELECT_ITEM, (byte) 0x80, offset);
		offset++;

		offset = createDevIdO(retBuf, offset);
		offset++;
//
//		// title menu items ?
		retBuf[offset] = (byte) 0x05;
		offset++;

		// Root Menu Text
		retBuf[offset] = (byte) SendEth.length;
		offset++;

		Util.arrayCopyNonAtomic(SendEth, (short) 0, retBuf, (short) offset, (short) SendEth.length);
		offset += SendEth.length;
		// TLV of 8f
		retBuf[offset] = (byte) 0x8f;
		offset++;
//
		retBuf[offset] = (byte) (AmountToSend.length + 1);
		offset++;
//
//		// item ID
		retBuf[offset] = (byte) 0x00;
		offset++;
//
		Util.arrayCopyNonAtomic(AmountToSend, (short) 0, retBuf, (short) offset, (short) AmountToSend.length);
		offset += AmountToSend.length;
//
		retBuf[offset] = (byte) 0x8f;
		offset++;
//
		retBuf[offset] = (byte) (AddressToSendTo.length + 1);
		offset++;
//
//		// item ID
		retBuf[offset] = (byte) 0x01;
		offset++;
//
		Util.arrayCopyNonAtomic(AddressToSendTo, (short) 0, retBuf, (short) offset, (short) AddressToSendTo.length);
		offset += AddressToSendTo.length;
//
		retBuf[offset] = (byte) 0x8f;
		offset++;
////
		retBuf[offset] = (byte) (Sign.length + 1);
		offset++;
//
//		// item ID
		retBuf[offset] = (byte) 0x02;
		offset++;
//
		Util.arrayCopyNonAtomic(Sign, (short) 0, retBuf, (short) offset, (short) Sign.length);
		offset += Sign.length;

		// Adding the BlindSig and CopySig menu item entries to the root menu
//
		retBuf[offset] = (byte) 0x8f;
		offset++;
////
		retBuf[offset] = (byte) (BlindSign.length + 1);
		offset++;
//
//		// item ID
		retBuf[offset] = (byte) 0x03;
		offset++;
//
		Util.arrayCopyNonAtomic(BlindSign, (short) 0, retBuf, (short) offset, (short) BlindSign.length);
		offset += BlindSign.length;
//
		retBuf[offset] = (byte) 0x8f;
		offset++;
////
		retBuf[offset] = (byte) (CopySig.length + 1);
		offset++;
//
//		// item ID
		retBuf[offset] = (byte) 0x04;
		offset++;
//
		Util.arrayCopyNonAtomic(CopySig, (short) 0, retBuf, (short) offset, (short) CopySig.length);
		offset += CopySig.length;
//

		// 0x10??
		retBuf[offset] = (byte) 0x10;
		offset++;

//		// 0x01 (Le)
		retBuf[offset] = (byte) 0x01;
		offset++;
		// 0x00 Value?
//		retBuf[offset] = (byte) 0x00;
//		offset++;

	}

	// param success = 0 failure =
	public short handleSetFetchDisplayText(byte[] retBuf, byte success) {
		byte offset = 0;

		retBuf[offset] = (byte) 0xd0;
		offset++;

//		//todo dyanmic
//		retBuf[offset] = (byte) (retBuf.length - 2);
		retBuf[offset] = (byte) 0x13;
		offset++;
//
		retBuf[offset] = (byte) 0x81;
		offset++;
		// Le of CMD is byte 7 should == 3
		retBuf[offset] = (byte) 0x03;
		offset++;
//
//
		retBuf[offset] = (byte) 0x01;
		offset++;

		retBuf[offset] = DISPLAY_TEXT;
		offset++;

		// different cmd qualifier
		retBuf[offset] = (byte) 0x80;
		offset++;

		retBuf[offset] = (byte) 0x82;
		offset++;
//
		retBuf[offset] = (byte) 0x02;
		offset++;

		retBuf[offset] = (byte) 0x81;
		offset++;

		// diff
		retBuf[offset] = (byte) 0x02;
		offset++;
//
		retBuf[offset] = (byte) 0x8d;
		offset++;

		// todo: or fail
//		retBuf[offset] = (byte) Success.length;
		retBuf[offset] = (byte) 0x08;
		offset++;

		retBuf[offset] = (byte) 0x04;
		offset++;

		// todo probalby this is Success.length - 1 (we put the char encoding inside
		// it's string
		Util.arrayCopyNonAtomic(Success, (short) 0, retBuf, (short) offset, (short) Success.length);
		// ret sw 9000
		return (short) 0x9000;

//

	}

	public void handleSetFetchGetInput(byte[] retBuf, byte getInputId) {

		short offset = 0;

		retBuf[offset] = (byte) 0xd0;
		offset++;
//
//		//todo dyanmic
		if (getInputId == 4) {
			retBuf[offset] = (byte) 0x81;
			offset++;

			retBuf[offset] = (byte) 0x9C;
		} else {
			retBuf[offset] = (byte) (retBuf.length - 2);
		}

		offset++;

//
		retBuf[offset] = (byte) 0x81;
		offset++;
		// Le of CMD is byte 7 should == 3
		retBuf[offset] = (byte) 0x03;
		offset++;
//
//
		retBuf[offset] = (byte) 0x01;
		offset++;
//
//
		retBuf[offset] = GET_INPUT;
		offset++;
//
		retBuf[offset] = (byte) 0x01;
		offset++;
//
		retBuf[offset] = (byte) 0x82;
		offset++;
//
		retBuf[offset] = (byte) 0x02;
		offset++;

		retBuf[offset] = (byte) 0x81;
		offset++;

		retBuf[offset] = (byte) 0x82;
		offset++;
//
		retBuf[offset] = (byte) 0x8d;
		offset++;
//

		switch (getInputId) {

		case AMOUNT_TO_SEND:
			// next byte should be TLV 82

			retBuf[offset] = (byte) (AmountToSend.length + 1);
			offset++;

//			//charset
			retBuf[offset] = (byte) 0x04;
			offset++;

			Util.arrayCopyNonAtomic(AmountToSend, (short) 0, retBuf, (short) offset, (short) AmountToSend.length);

			offset += AmountToSend.length;

			retBuf[offset] = (byte) 0x91;
			offset++;

			retBuf[offset] = (byte) 0x02;
			offset++;

			retBuf[offset] = (byte) 0x01;
			offset++;

//			retBuf[offset] = (byte) 0x24;
			retBuf[offset] = (byte) 0x80;
			offset++;

			// tag for default text
			retBuf[offset] = (byte) 0x97;
			offset++;

			// le
			retBuf[offset] = (byte) 0x08;
			offset++;

			retBuf[offset] = (byte) 0x04;
			offset++;

			retBuf[offset] = (byte) 'e';
			offset++;

			retBuf[offset] = (byte) 'a';
			offset++;

			retBuf[offset] = (byte) 'd';
			offset++;

			retBuf[offset] = (byte) 'b';
			offset++;

			retBuf[offset] = (byte) 'e';
			offset++;

			retBuf[offset] = (byte) 'e';
			offset++;

			retBuf[offset] = (byte) 'f';
			offset++;

			break;

		case ADDRESS_TO_SEND_TO:

			retBuf[offset] = (byte) (AddressToSendTo.length + 1);
			offset++;

//			//charset
			retBuf[offset] = (byte) 0x04;
			offset++;

			Util.arrayCopyNonAtomic(AddressToSendTo, (short) 0, retBuf, (short) offset, (short) AddressToSendTo.length);
			offset += AddressToSendTo.length;

			retBuf[offset] = (byte) 0x91;
			offset++;

			retBuf[offset] = (byte) 0x02;
			offset++;

			retBuf[offset] = (byte) 0x01;
			offset++;

			retBuf[offset] = (byte) 0x28;
			offset++;
			break;

		case ENTER_PIN:

			retBuf[offset] = (byte) (EnterPin.length + 1);
			offset++;

			// charset
			retBuf[offset] = (byte) 0x04;
			offset++;

			Util.arrayCopyNonAtomic(EnterPin, (short) 0, retBuf, (short) offset, (short) EnterPin.length);
			offset += EnterPin.length;

			retBuf[offset] = (byte) 0x91;
			offset++;

			retBuf[offset] = (byte) 0x02;
			offset++;

			retBuf[offset] = (byte) 0x01;
			offset++;

			retBuf[offset] = (byte) 0x28;
			offset++;

			break;

		case BLIND_SIGN:

			retBuf[offset] = (byte) (BlindSign.length + 1);
			offset++;

//			//charset
			retBuf[offset] = (byte) 0x04;
			offset++;

			Util.arrayCopyNonAtomic(BlindSign, (short) 0, retBuf, (short) offset, (short) BlindSign.length);
			offset += BlindSign.length;

			retBuf[offset] = (byte) 0x91;
			offset++;

			retBuf[offset] = (byte) 0x02;
			offset++;

			retBuf[offset] = (byte) 0x01;
			offset++;

			retBuf[offset] = (byte) 0x40;
			offset++;

			break;

		case COPY_SIG:

			retBuf[offset] = (byte) (CopySig.length + 1);
			offset++;

			// charset
			retBuf[offset] = (byte) 0x04;
			offset++;

			Util.arrayCopyNonAtomic(CopySig, (short) 0, retBuf, (short) offset, (short) CopySig.length);
			offset += CopySig.length;

			retBuf[offset] = (byte) 0x91;
			offset++;

			retBuf[offset] = (byte) 0x02;
			offset++;

			retBuf[offset] = (byte) 0x01;
			offset++;

			retBuf[offset] = (byte) 0x80;
			offset++;

			// todo: this never gets reached as a outputSig is initalized filled with 0s.
			if (this.wallet.outputSig == null) {

				// tag for default text
				retBuf[offset] = (byte) 0x97;
				offset++;

				retBuf[offset] = (byte) 0x07;
				offset++;

				retBuf[offset] = (byte) 0x04;
				offset++;

				// there is no sig in the output buffer.
				// a hash need signing
				retBuf[offset] = (byte) 'N';
				offset++;

				retBuf[offset] = (byte) 'o';
				offset++;

				retBuf[offset] = (byte) ' ';
				offset++;

				retBuf[offset] = (byte) 'S';
				offset++;

				retBuf[offset] = (byte) 'i';
				offset++;

				retBuf[offset] = (byte) 'g';
				offset++;

			} else {
//
//				//tag for default text
				retBuf[offset] = (byte) 0x97;
				offset++;

//				retBuf[offset] = (byte)(9 + (byte)this.wallet.outputSig.length);
//				offset++;
//
//				retBuf[offset] = (byte)0x04;
//				offset++;
//
//				Util.arrayCopyNonAtomic(this.wallet.outputSig, (short) 0, retBuf, (short) offset, (short) DERFormatLe);
//				offset += DERFormatLe;

				// strip out r&s points from the DER encoding
				byte[] derDecodedSig = this.wallet.stripDEREncoding(this.wallet.outputSig);
				// encode r&s as readable text
				byte[] gsmEncodedSig = this.wallet.convertHexArrayToGSM7(derDecodedSig, (byte) 0);

				// chars > 127
				retBuf[offset] = (byte) 0x81;
				offset++;

				retBuf[offset] = (byte) 129;
				offset++;

				retBuf[offset] = (byte) 0x04;
				offset++;

				Util.arrayCopyNonAtomic(gsmEncodedSig, (short) 0, retBuf, (short) offset, (short) 128);

			}
			break;
		}

	}

	public byte handleResponseSelectItem(byte v) {

//		// set next fetch response
//		this.selection = v;

		switch (v) {
		case ((byte) AMOUNT_TO_SEND):
			// todo: grab this from incoming apdu.
			// todo: make this temp buf?
			JCSystem.beginTransaction();
			if (this.nextFetchBuf != null) {
				JCSystem.requestObjectDeletion();
			}
			JCSystem.commitTransaction();

			// add 10 for default text length adding
			this.nextFetchBuf = new byte[AmtToSendLe];

			this.handleSetFetchGetInput(this.nextFetchBuf, AMOUNT_TO_SEND);

			return (byte) AmtToSendLe;

		case ((byte) ADDRESS_TO_SEND_TO):

			JCSystem.beginTransaction();
			if (this.nextFetchBuf != null) {
				JCSystem.requestObjectDeletion();
			}
			JCSystem.commitTransaction();

			this.nextFetchBuf = new byte[AddToSendToLe];

			this.handleSetFetchGetInput(this.nextFetchBuf, ADDRESS_TO_SEND_TO);

			return (byte) AddToSendToLe;

		case ((byte) ENTER_PIN):

			JCSystem.beginTransaction();
			if (this.nextFetchBuf != null) {
				JCSystem.requestObjectDeletion();
			}
			JCSystem.commitTransaction();

			this.nextFetchBuf = new byte[EnterPinLe];

			this.handleSetFetchGetInput(this.nextFetchBuf, ENTER_PIN);

			return (byte) EnterPinLe;

		case ((byte) BLIND_SIGN):

			JCSystem.beginTransaction();
			if (this.nextFetchBuf != null) {
				JCSystem.requestObjectDeletion();
			}
			JCSystem.commitTransaction();

			this.nextFetchBuf = new byte[BlindSignLe];

			this.handleSetFetchGetInput(this.nextFetchBuf, BLIND_SIGN);

//			this.wallet.importKey();
//			this.wallet.signWithKey(this.wallet.importedHash, this.outputSig);

			return (byte) BlindSignLe;

		case ((byte) COPY_SIG):

			JCSystem.beginTransaction();
			if (this.nextFetchBuf != null) {
				JCSystem.requestObjectDeletion();
			}
			JCSystem.commitTransaction();

//			try {
//				this.wallet.importKey();
//				this.wallet.signWithKey();
//			}catch(Exception e) {
//				ISOException.throwIt((short) 0x9160);
//			}
			this.selection = 4;

			short bufferLe;
			// allocate the buffer size here we already know if there will be a sig or not
			if (this.wallet.outputSig == null) {
				bufferLe = (short) 37;
			} else {
				// 2 * length to account for nibbles.
				bufferLe = (short) (29 + (2 * this.wallet.outputSig.length));

			}
//			bufferLe = 31;
			try {
				this.nextFetchBuf = new byte[bufferLe];

			} catch (ISOException e) {
				ISOException.throwIt((short) 0x9199);
			}

			this.handleSetFetchGetInput(this.nextFetchBuf, COPY_SIG);

			return (byte) (31 + 128);

		}

		return (byte) 0x00;

	}

	public void handleResponseGetInput(byte[] input, byte selection) {
		// parse out input string

		switch (selection) {

		case ((byte) AMOUNT_TO_SEND):
//			this.inputAmt = JCSystem.makeTransientByteArray((short) input.length, JCSystem.CLEAR_ON_RESET);
			this.inputAmt = new byte[(byte) input.length];
			Util.arrayCopyNonAtomic(this.inputAmt, (short) 0, input, (short) 0, (short) input.length);
			// length of success string.

			this.nextFetchLe = (byte) 21;

			this.nextFetchBuf = JCSystem.makeTransientByteArray((short) this.nextFetchLe, JCSystem.CLEAR_ON_RESET);
//
			handleSetFetchDisplayText(this.nextFetchBuf, (byte) 0);
			break;

		case ((byte) BLIND_SIGN):
//			this.inputAddress = JCSystem.makeTransientByteArray((short) input.length, JCSystem.CLEAR_ON_RESET);
			// todo should just be 32
			this.inputHash = new byte[(byte) input.length];

			Util.arrayCopyNonAtomic(this.inputHash, (short) 0, input, (short) 0, (short) input.length);

			this.nextFetchLe = (byte) 15;
			break;

		case ((byte) ADDRESS_TO_SEND_TO):
//			this.inputAddress = JCSystem.makeTransientByteArray((short) input.length, JCSystem.CLEAR_ON_RESET);
			this.inputAddress = new byte[(byte) input.length];
			Util.arrayCopyNonAtomic(this.inputAddress, (short) 0, input, (short) 0, (short) input.length);
			this.nextFetchLe = (byte) 15;
			break;

		// todo handle pin as transient. etc.
		case ((byte) ENTER_PIN):
//			this.inputPin = JCSystem.makeTransientByteArray((short) input.length, JCSystem.CLEAR_ON_RESET);
			this.inputPin = new byte[(byte) input.length];
			Util.arrayCopyNonAtomic(this.inputPin, (short) 0, input, (short) 0, (short) input.length);

			this.nextFetchLe = (byte) 15;
			break;
		}

	}

	// this is a response from the terminal to the card, and indicate selection of
	// items,
	public short handleTerminalResponse(APDU apdu) {
		// terminal response Get Item
		short sw = (short) 0x9000;

		byte offset = 1;

		byte buf[] = apdu.getBuffer();
		//
		byte totalLen = buf[4];

		// take incoming bytes and break it out.
		// this.offset = ISO7816.OFFSET_INS + 4;
//		this.tempBuf = JCSystem.makeTransientByteArray((short) (totalLen+2), JCSystem.CLEAR_ON_RESET);
		// annoyingly need to init temp buf this way.

		// todo change back to RAM allocation
//		this.tempBuf = new byte[(byte) (totalLen + 32)];

		this.tempBuf = JCSystem.makeTransientByteArray((short) (totalLen + 2), JCSystem.CLEAR_ON_RESET);
//
//		// copy the incoming to tempbuf.
		Util.arrayCopyNonAtomic(buf, (short) 4, this.tempBuf, (short) 0, (short) (totalLen + 1));
//
		this.nextFetchLe = (byte) (totalLen + 2);

//		// should be 81
		byte cmdDetailTag = this.tempBuf[offset];
		offset++;
		// should always be 3
		byte cmdDetailLe = this.tempBuf[offset];
		offset++;
//
//		// usually 1
		byte cmdNumber = this.tempBuf[offset];
		offset++;
//		// select etc
		byte cmdType = this.tempBuf[offset];
		offset++;

		switch (cmdType) {
		// was initially failing cause its shorter than other TRs.
		case (DISPLAY_TEXT):
			//A response to the display text diag.
			//return the length of the root menu
			sw = Util.makeShort((byte) 0x91, (byte) 0x60);
			//set root menu selection.
			this.selection = 0;

			return sw;
		}

		byte cmdQualifier = this.tempBuf[offset];
		offset++;
//
//		// devid
//		// should be 02
		byte devIdTag = this.tempBuf[offset];
		offset++;
//
		byte devIdLe = this.tempBuf[offset];
		offset++;
//
//		// 81/82 SIM <> TERMINAL
		byte devFromId = this.tempBuf[offset];
		offset++;
//
		byte devToId = this.tempBuf[offset];
		offset++;
//
//		// Result TLV
		byte resTag = this.tempBuf[offset];
		offset++;
//
		byte resLe = this.tempBuf[offset];
		offset++;
//
		byte resRes = this.tempBuf[offset];
		offset++;

//		//type 90 is sleect item
		// type 8d for text
		byte trTagType = this.tempBuf[offset];
		offset++;
//
		byte trTagLe = this.tempBuf[offset];
		offset++;

		// tag value or first char encoding
		byte trTagV = this.tempBuf[offset];
		offset++;

		byte nextLe = (byte) 33;

		// parse out cmd type
		switch (cmdType) {
		case (SELECT_ITEM):
			nextLe = this.handleResponseSelectItem(trTagV);
			sw = Util.makeShort((byte) 0x91, nextLe);
			break;

		case (GET_INPUT):

			this.inputAmt = new byte[(byte) (trTagLe - 1)];
			// why do I need to add 4 to offset here? dosent register
			Util.arrayCopyNonAtomic(buf, (short) (offset + 4), this.inputAmt, (short) 0, (short) (trTagLe - 1));

			if (resRes == 0) {
				// get the input from the TR response
				handleResponseGetInput(this.inputAmt, (byte) 0);
//				byte[] tmp = new byte[(byte)20];

				// set the fetch to be the success
//				this.offset = 0;
//				handleSetFetchDisplayText(tmp, (byte)0);

				sw = Util.makeShort((byte) 0x91, (byte) 0x15);
			} else {
				sw = Util.makeShort((byte) 0x91, (byte) Fail.length);
			}
//			sw = (short) 0x9199;
			break;

		case (DISPLAY_TEXT):
			// success length at least
			// this.nextFetchBuf = new byte[(byte) 0x15];
			// success
//			sw = handleSetFetchDisplayText(this.nextFetchBuf, (byte) 0x00);

			// okay pressed
			// if(resRes == 0x10) {
			// rset to initial fetch
			// this.initialFetch = true;
			// sw to the length of the initial fetch
			sw = Util.makeShort((byte) 0x91, (byte) 0x60);

			// }

			break;

		}
		return sw;

	}

	public void handleFetch(APDU apdu) {

		byte buf[] = apdu.getBuffer();

		short totalLen = buf[ISO7816.OFFSET_INS + 3];

		// init tlv stuff
		short retLen = (short) (totalLen - 2);

		// initial fetch will be initial STK Services MENU
		if (this.initialFetch == true) {
			this.tempBuf = JCSystem.makeTransientByteArray(totalLen, JCSystem.CLEAR_ON_RESET);

			this.handleMenuSetup(this.tempBuf, (byte) retLen);

			this.initialFetch = false;

			apdu.setOutgoing();
			apdu.setOutgoingLength((short) this.tempBuf.length);
			apdu.sendBytesLong(this.tempBuf, (short) 0, (short) this.tempBuf.length);
			// returns 9000
			return;
		} else if (this.selection == 0) {
			this.tempBuf = JCSystem.makeTransientByteArray(totalLen, JCSystem.CLEAR_ON_RESET);
			// if the selection is 0 indicate that the root menu has been chosen

			this.handleRootFetch(this.tempBuf, (byte) retLen);
			this.selection++;
//
//			this.stkSelected = true;
			apdu.setOutgoing();
			apdu.setOutgoingLength((short) this.tempBuf.length);
			apdu.sendBytesLong(this.tempBuf, (short) 0, (short) this.tempBuf.length);
//
			return;
			// next else handle everyting not initial selection
		} else if (this.selection == 4) {
			// everything else should have been triggered by a terminal response (ie ITEM
			// SELECT) and
			// the next fetch should have been precomputed and saved globally.
			apdu.setOutgoing();
			apdu.setOutgoingLength((short) (31 + 128));
			apdu.sendBytesLong(this.nextFetchBuf, (short) 0, (short) (31 + 128));

		} else {

			// everything else should have been triggered by a terminal response (ie ITEM
			// SELECT) and
			// the next fetch should have been precomputed and saved globally.
			apdu.setOutgoing();
			apdu.setOutgoingLength((short) this.nextFetchBuf.length);
			apdu.sendBytesLong(this.nextFetchBuf, (short) 0, (short) this.nextFetchBuf.length);

		}

		return;
	}
}
