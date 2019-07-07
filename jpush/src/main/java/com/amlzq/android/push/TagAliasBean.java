package com.amlzq.android.push;

import java.util.Set;

/**
 * 别名和标签
 */
public class TagAliasBean {
    public int action;
    public Set<String> tags;
    public String alias;
    public boolean isAliasAction;

    @Override
    public String toString() {
        return "TagAliasBean{" +
                "action=" + action +
                ", tags=" + tags +
                ", alias='" + alias + '\'' +
                ", isAliasAction=" + isAliasAction +
                '}';
    }

}
