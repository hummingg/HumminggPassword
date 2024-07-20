package com.hummingg.humminggpassword;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordUtils {

    // 密码字符集
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_=+[]{}|;:',.<>?/";

    // 密码生成所需字符集
    private static final String ALL_CHARACTERS = LOWERCASE + UPPERCASE + DIGITS + SPECIAL_CHARACTERS;


    private static SecureRandom random = new SecureRandom();

    /**
     * 生成符合要求的随机密码
     *
     * @return 随机密码
     */
    public static String generatePassword(int minLength) {
        StringBuilder sbPassword = new StringBuilder(minLength);

        // 确保密码包含至少一个字符类别
        sbPassword.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        sbPassword.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        sbPassword.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        sbPassword.append(SPECIAL_CHARACTERS.charAt(random.nextInt(SPECIAL_CHARACTERS.length())));

        // 填充剩余的密码字符
        for (int i = sbPassword.length(); i < minLength; i++) {
            sbPassword.append(ALL_CHARACTERS.charAt(random.nextInt(ALL_CHARACTERS.length())));
        }

        // 打乱字符顺序
        List<Character> characters = new ArrayList<>(sbPassword.length());
        for(int i=0; i<sbPassword.length(); i++){
            characters.add(sbPassword.charAt(i));
        }
        Collections.shuffle(characters);
        StringBuilder shuffledPassword = new StringBuilder();
        for (char c : characters) {
            shuffledPassword.append(c);
        }

        return shuffledPassword.toString();
    }

}

