package com.pawel.p7_go4lunch.model.autocomplete;

public class Matched_substrings {
    private String offset;

    private String length;

    public String getOffset ()
    {
        return offset;
    }

    public void setOffset (String offset)
    {
        this.offset = offset;
    }

    public String getLength ()
    {
        return length;
    }

    public void setLength (String length)
    {
        this.length = length;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [offset = "+offset+", length = "+length+"]";
    }
}
