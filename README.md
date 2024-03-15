# Semaphore Network EthSIM Wallet
![](https://github.com/base0010/semaphore_network_ethsim/blob/main/logo0.png)


### DO NOT USE TO STORE VALUABLES. VERY MUCH A WORK IN PROGRESS

## What?
The Semaphore Network EthSIM Wallet aims to allow anyone to turn ANY cellphone into a secure air-gapped hardware wallet for Ethereum transactions.

## TLDR Features:
*  Novel first (afaik) partial raw re-implementation of SIM Toolkit (STK) features, without the sim.*/uicc.* proprietary API ROM blob
*  allows ANY Javacard to be used. NO TELCO GATEKEEPING
*  The ability to display menus, [hidden]inputs and outputs that create a Hardware Wallet UI interface directly on-card.
*  Leverages Secure Element for keygen, signing, PIN etc.

## Future:
- [-] hashing on card.
- [-] tx/rlp generation on card.

## How?
By combining a cheap(~$10) secure element SIM-sized JavaCard/Smarcard and almost ANY cellphone;
The Semaphore Network EthSIM platform enables an extremely low-cost, secure and open hardware wallet experience.

![](https://github.com/base0010/semaphore_network_ethsim/blob/main/animation.gif)

## Background:
Almost everyone knows about the SIM card that goes in your phone, billions are produced annually and as such are probably one of the most ubiquitous electronic devices in the world. What remains elusively packed away inside that SIM card, is that it runs a subset of a secure computing platform called Javacard.

Javacard is a predominant smartcard and secure element platform used by many manufacturers; if you use a hardware wallet or secure element today chances are that you're using a Javacard at some level. Grid+, Status Keycard, Yubikey (partial), Ledger (partial) and Kong all use Javacard secure elements at their core.

So clearly a Javacard, providing the right software can do both **crypto stuff** AND **phone stuff**.
The phone stuff is important to Semaphore Network as we build out a truly decentralized cellular network.
However, a key differentiator of our network is using self-sovereign Ethereum cryptographic identity at the core network layer.

The management of these keys can be cumbersome, especially if the phones of a truly global audience might not even have an app store.

Therefore, we needed a truly universal way to deliver UI to our users, something absent app stores, walled gardens etc.
It just so happened that on our quest to give power back to the people, we stumbled across a piece of ancient telco knowledge...

## SIM Toolkit (STK):
A little-known protocol for creating simple apps -
something that predates app stores by about a decade -
The ability to display menus, [hidden] input and output built into literally every phone since the mid-90s!

You might remember it from feature phones of yesteryear. Maybe on that European trip in 2005, you bought some more minutes through an obscure payment menu embedded deep into your Moto Razr...That, my friend, was a SIM Toolkit.

So, what's the big deal? Until now, the intersections of phone Javacard technology and crypto Javacard technology were out of reach. The SIMToolkit API is proprietary; you have to be telco adjacent, know a card manufacturer and/or have large MOQ orders AND pay extra to have this feature included in custom Javacards.

Except, not anymore! We've started a byte-level rewrite of this functionality and are sharing it here in our hardware wallet implementation.

## Basic Functionality
The ETHSIM companion app [https://github.com/base0010/ethsim-companion] is designed to create more advanced transaction types that can be put on EVM-compatible chains. Outside very basic transaction types, "Send ETH", and "Show Public Key (APDU)" the ETHSIM app will also accept "blind signed" transaction types that will allow you to do any arbitrary EVM chain interaction. We have basic support for sending built-in tokens. Feel free to add more in a PR. 

### Sending ETH to Address (via SIM applet): 
**Input:  "Amount to Send"** - input the amount of ETH you wish to send.  

**Input:  "Address to Send to"** - the address to send ETH to. (prefixed with 0x)  


-> **Select the Sign** - button and input PIN (if enabled)  

-> **Select the "Copy Sig"** - button to display the final signature that will be put on chain.  


**Copy the signature across the airgap**
-> Send the signature to the chain via RPC or companion webapp.


### Blind Signature (via webapp/external): 
Use this option to perform more advanced transaction types and smart contract interactions. And uses the companion application to generate a hash which will be signed with the STK applet.

**EthSIM companion webapp**

**Input:  "Amount to Send"** - input the amount of ETH you wish to send.  

**Input:  "Address to Send to"** the address to send ETH to. (prefixed with 0x)  

**Input:  "Token Selection"** - select the token type you wish to send  


**Copy Hash:** Copy the transaction hash generated in the webapp to the air-gapped device  


**Input: "Blind Sign"** - input the hash from the previous step into the Blind Sign prompt in the SIM applet.  


  -> **Select the Sign button** and input PIN (if enabled)  
  
  -> **Select the "Copy Sig"** button to display the final signature that will be put on chain. 



## Limitations:
There are many pros and cons
