�}�\    �1��ah4͠    ��'/����㿮�HK�l�����AE��"x��36&i��q:z�j���Z�J�5�2Y���P_�'���z_�A��5T��d+�4��T�z��mdT��]�����L�If�a��&-�>ॲ���~��9�e[ӹ(�9�.��+=�ey��q��#��F�b�3���g�&ݰ�I���?҂.��H1>)֔'fԅ�!��ŋ���媿��Sʛ��Y;�����Id��
������B�x���Snk�C����NV�d';��������ˉ����;w�~�N��)4��׸,C�u*i��
,g�ڟ��i�D���:�\F��c�%�`X��_텴�`�?`c�J����GGj�y+? ����I�r�R�Tr��x�	.�ALe��m��'�@�t��z��\J0u��[3�F�F�ӿ�/�����.�;2�tgWe�Y����p�A����]/�W�-�?����Ѿ��9Xf�"Q�l,����1���p�:R���#� ���\ǯU�q��:��m�B~G믍6��Y��� #�a'>�߯`�x�oU+3��t��(�8�i
�8|�^M��9�r�&ݦ^*�LN��C�DYB�z�N��l1)Sz���z��
��u��A��lN�d�gsd�i���H&�lQ��D3A!>�qL�j����-e�W�PO�X\��w�f���{�ΌVZ@D �g^��<VD��P��<�?��e4��}%^���2cʹ��g������������(�����g�+� p���o+���;���-�.��w����(�
#��#[j��-���J��a)9LՓl�IfT����A,3�� ��U���v:��"�h-k���h ��ҦQ(H(�"O�6Wa�zc�Y9��x]<�9	�б��f*�fLr�<����#�0G��قT;����G�	�?�f�����Հ6�������� g%���+T�Y�gk�S�xD2NP�Π'� 7�F�xD���:��l�)v����7{�
��i��K�B�����wP�7�I�tv_�|��g�j��ۤ7��b��٭������>E�r��҇����Ϲ�r!���ଦ�q^�e�(�U�L���W����;�n�r���1d����g�:g�B����n6N�L���"���t�i��4~?9h��oz}H7����D۷w˝GD\�=�)-Ē�֍���.�ז,)�@���/��>��,܋���������~a��� ht�*#�K2����B����ѭ���J}.�\��bM� ���p�q�
�<Si�ouv61�q�O�0�L(�+M�wC��d_��Ta���K�N�0���#�oi��r���m˳����<��+d#'�*�[6=�?�(E�(�+.1��h�>=%Je��t,ERԎ<"��~�4�^*����>�(�iC&���ǻT9=w�8���V�/��A� ��8r8e��6��h�uЏ��<��؂lR(��eTableEntry(int index) {
    long r = (long) index;

    if (Parameters.RefIn) {
      r = CrcHelper.ReverseBits(r, HashSize);
    } else if (HashSize > 8) {
      r <<= (HashSize - 8);
    }

    long lastBit = (1L << (HashSize - 1));

    for (int i = 0; i < 8; i++) {
      if ((r & lastBit) != 0) {
        r = ((r << 1) ^ Parameters.Poly);
      } else {
        r <<= 1;
      }
    }

    if (Parameters.RefOut) {
      r = CrcHelper.ReverseBits(r, HashSize);
    }

    return r & _mask;
  }
}
