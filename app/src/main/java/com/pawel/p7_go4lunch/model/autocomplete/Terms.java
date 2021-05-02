package com.pawel.p7_go4lunch.model.autocomplete;

public class Terms {
    private String offset;

    private String value;

    public String getOffset ()
    {
        return offset;
    }

    public void setOffset (String offset)
    {
        this.offset = offset;
    }

    public String getValue ()
    {
        return value;
    }

    public void setValue (String value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [offset = "+offset+", value = "+value+"]";
    }
}
