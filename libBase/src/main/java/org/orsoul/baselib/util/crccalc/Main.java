�}]\  
  ����]�\    ��+/�����ϲ�H�ݡrz�Q�"&L�.Ю�_���!Lv�\���u6�E����_���w�\@��s\����e���&l���r���fR(��*��D?_�[��I=[��!Ki-x6�-$�"x;�Oq��5Kd(�uH)]��^V��F�f�܃؆���qĈ���� (b0@0\��o1j�F�$v�aϏZg����_�9�����@��n3��\H�T�e m�"���҂Z�׭*tTh �2p�eM0�IG�64c�;5I���b��($��D1�Eǐ����׸,C�u*i��
,g�ڟ��i�D���:�\F��c�%�`X��_텴�`�?`c�J����GGj�y+? ����I�r�R�Tr��x�	.�ALe��m��'�@�t��z��\J0u��[3�F�F�ӿ�/�����.�;2�tgWe�Y����p�A����]/�W�-�?����Ѿ��9Xf�"Q�  //Check(Crc32.Params);
    //Check(Crc64.Params);
  }

  private static void Check(AlgoParams[] params) {
    for (int i = 0; i < params.length; i++) {
      CrcCalculator calculator = new CrcCalculator(params[i]);
      long result = calculator.Calc(CrcCalculator.TestBytes, 0, CrcCalculator.TestBytes.length);
      if (result != calculator.Parameters.Check) {
        out.println(calculator.Parameters.Name + " - BAD ALGO!!! " + Long.toHexString(result)
            .toUpperCase());
      }
    }
  }
}