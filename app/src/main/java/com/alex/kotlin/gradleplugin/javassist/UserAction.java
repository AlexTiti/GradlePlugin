package com.alex.kotlin.gradleplugin.javassist;

/**
 * @author Alex
 * @date 2020-01-02 16:08
 * @email 18238818283@sina.cn
 * @desc ...
 */
public class UserAction {
    private String mUserName;
    private int mAge;

    public UserAction() {
    }

    public UserAction(String mUserName, int mAge) {
        this.mUserName = mUserName;
        this.mAge = mAge;
    }

    public void action(String something) {
        System.out.println("123");
    }
}
