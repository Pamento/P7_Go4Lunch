package com.pawel.p7_go4lunch.model.autocomplete;

public class Structured_formatting
{
    private Main_text_matched_substrings[] main_text_matched_substrings;

    private String secondary_text;

    private String main_text;

    public Main_text_matched_substrings[] getMain_text_matched_substrings ()
    {
        return main_text_matched_substrings;
    }

    public void setMain_text_matched_substrings (Main_text_matched_substrings[] main_text_matched_substrings)
    {
        this.main_text_matched_substrings = main_text_matched_substrings;
    }

    public String getSecondary_text ()
    {
        return secondary_text;
    }

    public void setSecondary_text (String secondary_text)
    {
        this.secondary_text = secondary_text;
    }

    public String getMain_text ()
    {
        return main_text;
    }

    public void setMain_text (String main_text)
    {
        this.main_text = main_text;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [main_text_matched_substrings = "+main_text_matched_substrings+", secondary_text = "+secondary_text+", main_text = "+main_text+"]";
    }
}
