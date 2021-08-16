package org.orsoul.baselib.util;

import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.DesUtil;

import org.junit.Assert;
import org.junit.Test;

public class DesUtilTest {

  @Test
  public void cipherDoFinal() throws Exception {
    String text = "哈喽12ab_#";
    String pwd = "123";
    byte[] dataEncrypt;
    byte[] dataDecrypt;

    /* 测试 DES */
    dataEncrypt = DesUtil.cipherDoFinal(text, pwd, true, "DES/ECB/PKCS5Padding");
    dataDecrypt = DesUtil.cipherDoFinal(dataEncrypt, pwd, false, "DES/ECB/PKCS5Padding");
    Assert.assertEquals("363a257d02a8aa7755720eee307a11b4",
        BytesUtil.bytes2HexString(dataEncrypt).toLowerCase());
    Assert.assertEquals(text, new String(dataDecrypt, "utf-8"));

    /* 测试 3DES */
    dataEncrypt = DesUtil.cipherDoFinal(text, pwd, true, "DESede/ECB/PKCS5Padding");
    dataDecrypt = DesUtil.cipherDoFinal(dataEncrypt, pwd, false, "DESede/ECB/PKCS5Padding");
    Assert.assertEquals("363a257d02a8aa7755720eee307a11b4",
        BytesUtil.bytes2HexString(dataEncrypt).toLowerCase());
    Assert.assertEquals(text, new String(dataDecrypt, "utf-8"));

    /* 测试 AES */
    dataEncrypt = DesUtil.cipherDoFinal(text, pwd, true, "AES/ECB/PKCS5Padding");
    dataDecrypt = DesUtil.cipherDoFinal(dataEncrypt, pwd, false, "AES/ECB/PKCS5Padding");
    Assert.assertEquals("34e59bc48dfd337ff84d12fb7d10abb0",
        BytesUtil.bytes2HexString(dataEncrypt).toLowerCase());
    Assert.assertEquals(text, new String(dataDecrypt, "utf-8"));

    text =
        "AES,高级加密标准（英语：Advanced Encryption Standard，缩写：AES），在密码学中又称Rijndael加密法，是美国联邦政府采用的一种区块加密标准。这个标准用来替代原先的DES，已经被多方分析且广为全世界所使用。严格地说，AES和Rijndael加密法并不完全一样（虽然在实际应用中二者可以互换），因为Rijndael加密法可以支持更大范围的区块和密钥长度：AES的区块长度固定为128 比特，密钥长度则可以是128，192或256比特；而Rijndael使用的密钥和区块长度可以是32位的整数倍，以128位为下限，256比特为上限。包括AES-ECB,AES-CBC,AES-CTR,AES-OFB,AES-CFB";
    pwd = "密码 cd_#!！";
    /* 测试 DES */
    dataEncrypt = DesUtil.cipherDoFinal(text, pwd, true, "DES/ECB/PKCS5Padding");
    dataDecrypt = DesUtil.cipherDoFinal(dataEncrypt, pwd, false, "DES/ECB/PKCS5Padding");
    Assert.assertEquals(
        "b48ca7704cd5b4e3a2844beaae5951d7f9566794aad74950b11501aa9c60560c26d9d2510332dfc42a9ccd605f92773d565e861a8d1cab6639b585013058ccecd68b7cce154f0365825afc0ebc1a3ad62ef045c4dca385a8f424411aacc1e6dc2797ee776edd47727a82d9e2ac41904c161d9a2f3a57222215642399add2dc802fb2ca25c23139ebfd638aabea231cc9b6a9511ab44fb5bdd7a9249ba1db62d33bdb87ce2a7b0c699a4c3e741c26bf446599031c7e0048f75983e090d0e297fdc4cf985e3e7c855ec7690fc297458735079fbfd5dd6789d319b03c21731bd173f6b70a7be6c8d16142e361413f2aa5b39007e6e3950771008c49f2c4c40c689b41596a7ad350e0d538b8a3ab73b4c2978d09afdebe138ed6a1ab35ee2a28707016ac7117e75735bd5faec4b37931b6d4664c17ad8c450b55e50c81f5c79f4e79de4645dedd3638b572991fc714b5288e76551f9dcf20dc190395a193eecac2ff717f66e38d658eb3accf4ec40110bb9ef573034e97e4b3280f99f552497f42007a82d9e2ac41904c161d9a2f3a5722228155b8dd58fdaf231b54b63628a4957c8079fc3d0897cba764b073eae66e30844934fc423d0c3e578eb548e6da1889648b29bb922187528ae46a6940c082d55bd5ff3a08667b3042590bc1c5a6d0329939c57415880c3c5624d72b00103df0a38f565e3e8328e2838d192aface7b573686fccdc6f70614b1269adab611c3c0555625b68da8a6ddcc45e16c88b43547c1c0a8dc89a85309d6d12eb780d37d39aa0360757a4b2e031fa51a909431c110e37ed1e5d5e2affa65ee796aecb37e3aa01f9a2a20fa7344c2fe85145bd73f56bcd4488da90438d0f88507ec35abe1a00be67acf335acc355cc2a5e9689de24be22c8ca60d401442d091f4590a78c95bbeb9d1c4b2bb53f53c6ab0dfaf0eded0c56e5553097b374da1ab87d45c5e6e9a19d80a53ad1e27df4016a0fad7bafa38aa",
        BytesUtil.bytes2HexString(dataEncrypt).toLowerCase());
    Assert.assertEquals(text, new String(dataDecrypt, "utf-8"));

    /* 测试 3DES */
    dataEncrypt = DesUtil.cipherDoFinal(text, pwd, true, "DESede/ECB/PKCS5Padding");
    dataDecrypt = DesUtil.cipherDoFinal(dataEncrypt, pwd, false, "DESede/ECB/PKCS5Padding");
    Assert.assertEquals(
        "c0ccd8cbe67902f40d38d5d1d432c1354b92080c1129d9091e82ed1f1c622dfb668b93dda2f4616b99fbc54a47be80a7b99f3be72f06d207f8edc5657b9dd7a98f014af0d0c3193e45233e85f57fb890595bea5939da28a01ae34320ba495eda05a504fdd0c77107ea5f896ad6d693e61a7fb126e76d90d006b1c866fca6101cb00716bbba50dd06736e1e16117ddf74fecfd9835a3a20e5e516d2ab7434a9c7a37bcc57cb5d1b525c88d617962699afd947ed9ae2864e502f63d4f47b7c731e6484380c637538a51437fd819b221489f376bdf6a70146dac5a8f68d2ada979e8956bb772ba4439a660a8167c0bb09e4aee8a663b37dbf04adc2b1af7f4a4b06d55b15ebcc69e8a6eb77acbffb81b34c394d86c8989978d27a52d383fd94675f2878b46174f77ef906b320f523f06ea88d1f7448d0142706b552fbd68eaca6de3e68b30e22243d6a5b0003c73859bcf72fc3122e3f3b32c942573e58e5775dde6acd00e6637e2b7935aa9b6f51f60363206ae466b1922dfc1f716a0ffee4daa1ea5f896ad6d693e61a7fb126e76d90d0beebc53e3e5242bc8430e063e3e4f81c2798c499e67fc2782f8ef8e3ea3c91509749cf0b7337b587e33c69b456e47e7278deb56642cfb7f05f9d86c8d1a31b79a241645cd85571919799128d99068552efc8e9eb5cc10391198c535122432fe11181518f8af981cbdafc67b5c34754b825b0ecff7647418173d8d1e50d917f4c95a96522ed030f038e62705cdb109adea38d9e257f91165f9ee8bc42793ec7cbf1064025cfa9e204128e387f16d497334ad0feebc88e09fe59581bd9250dc2a5ae336b86154dce8daca9c7aa50510f4025e520034232bbc503a37594c94455fb7a64d61c254c61733be5a3507dab90ac1ef58ec996f646cda53998007c0bf17c47de7a54fedb2ab762ea44f44d3bad53ae7cc0feafc0cae0304605e6705259aba2c93e5a80b14218174607d35bf3122c",
        BytesUtil.bytes2HexString(dataEncrypt).toLowerCase());
    Assert.assertEquals(text, new String(dataDecrypt, "utf-8"));

    /* 测试 AES */
    dataEncrypt = DesUtil.cipherDoFinal(text, pwd, true, "AES/ECB/PKCS5Padding");
    dataDecrypt = DesUtil.cipherDoFinal(dataEncrypt, pwd, false, "AES/ECB/PKCS5Padding");
    Assert.assertEquals(
        "788d51d992cf1a62f9b02eb627b2fb3b79da06664ac1c9da3cdde2c61de936cb9ff59cd00334820ef676ebbc211b36785de258d297905620ead9fc93d391c58cb6baec341aea090358ff217dc8d3fcbdb1454ee6f6ef1fdb4e2cb0515b1eff736c80c59b2a9819a4f1694ca18e653eaca553f2e6b288f40a0a6192c798e8cddf42e2ffd1dc5be84c00cd37c4b5b88adff0add34f55e0c7078658c55ea4419f634e40490822673c11ae9d17e1db597bd0706a47a3c799764f08e5e5378f67e635cb046021bf3d61a638bcdfeea56e5fda34076fc1a69ebde6c6f7c57c3833b904ecc97753f968f4821a5557149679391412194c736bf629f6245b748a4ac50566e6d8f55655c0f277ad402ca40c509fdb8e90e3514b150e0cbdcc57244f9d5fe16fd17bd8b1bc1c0bfb07cc7d77b7130effcef740c611e567d90aca2edb4e04a617df53faf507454013a913218408552a6a93d3e045892c4c703541cf556032a6b866ee0b89b1f62e689f31bd44d110771927d275d5ef4dc3c57beb354063d2267048919f89b3e5d8a56525bf8b9dcd9168b1e966676f82c367ad16022b1da8d5a6364de4abead37d69060074e61ca833053c3f9b54b0fd808abaac721c1988687b90073e300c17537bfa43fbc8be9ceea448f87dbb5a8dd22d78f642d71db146683b258df71030e82b739d3c06b271bca4b46de55c2019dbba5be6ec22c21a4a4797d9b28dd39c5ba5d4b9bbad7ac32fa0a4eea04f34d395315a9ff22a8a15c8daa6da5919361615ea396fe8487580a829ba234dc7748f654e0c0790a9c10fd3002f9e9f96ff2b3524b10eca708e2935cf36d77c334e7c03a3665dfc9408d852e42d133b8aada845a0b09620571d3da2852ece288ce520e9ecccd4ea22995119e05ed2091199c307e9cd3e290adf299726d6be198ac4cdad60fb6fd2538730d38e8a229166117f99b72aeab0204e811e092871a062f2de013441c89b54357109",
        BytesUtil.bytes2HexString(dataEncrypt).toLowerCase());
    Assert.assertEquals(text, new String(dataDecrypt, "utf-8"));
  }
}