package com.improvedoverheads;

public enum OverheadsNumberFormat
{
    CURRENT("Current"),
    CURRENT_MAXIMUM("Current / maximum"),
    PERCENTAGE("Percentage");

    private final String label;

    OverheadsNumberFormat(String label)
    {
        this.label = label;
    }

    String format(int current, int maximum)
    {
        int value = Math.max(0, current);
        switch (this)
        {
            case CURRENT_MAXIMUM:
                return value + "/" + (maximum > 0 ? maximum : "?");
            case PERCENTAGE:
                return maximum > 0 ? Math.round(value * 100.0 / maximum) + "%" : "?";
            default:
                return Integer.toString(value);
        }
    }

    @Override
    public String toString()
    {
        return label;
    }
}
