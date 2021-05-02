package com.pawel.p7_go4lunch.model.autocomplete;

public class Predictions {
    private String reference;

    private String[] types;

    private Matched_substrings[] matched_substrings;

    private String distance_meters;

    private Terms[] terms;

    private Structured_formatting structured_formatting;

    private String description;

    private String place_id;

    public String getReference ()
    {
        return reference;
    }

    public void setReference (String reference)
    {
        this.reference = reference;
    }

    public String[] getTypes ()
    {
        return types;
    }

    public void setTypes (String[] types)
    {
        this.types = types;
    }

    public Matched_substrings[] getMatched_substrings ()
    {
        return matched_substrings;
    }

    public void setMatched_substrings (Matched_substrings[] matched_substrings)
    {
        this.matched_substrings = matched_substrings;
    }

    public String getDistance_meters ()
    {
        return distance_meters;
    }

    public void setDistance_meters (String distance_meters)
    {
        this.distance_meters = distance_meters;
    }

    public Terms[] getTerms ()
    {
        return terms;
    }

    public void setTerms (Terms[] terms)
    {
        this.terms = terms;
    }

    public Structured_formatting getStructured_formatting ()
    {
        return structured_formatting;
    }

    public void setStructured_formatting (Structured_formatting structured_formatting)
    {
        this.structured_formatting = structured_formatting;
    }

    public String getDescription ()
    {
        return description;
    }

    public void setDescription (String description)
    {
        this.description = description;
    }

    public String getPlace_id ()
    {
        return place_id;
    }

    public void setPlace_id (String place_id)
    {
        this.place_id = place_id;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [reference = "+reference+", types = "+types+", matched_substrings = "+matched_substrings+", distance_meters = "+distance_meters+", terms = "+terms+", structured_formatting = "+structured_formatting+", description = "+description+", place_id = "+place_id+"]";
    }
}
