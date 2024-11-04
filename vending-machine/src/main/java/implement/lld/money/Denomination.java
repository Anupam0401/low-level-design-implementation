package implement.lld.money;

import implement.lld.exception.UnknownDenominationException;

public enum Denomination {
    ONE(1),
    TWO(2),
    FIVE(5),
    TEN(10),
    TWENTY(20),
    FIFTY(50),
    HUNDRED(100),
    TWO_HUNDRED(200),
    FIVE_HUNDRED(500),
    THOUSAND(1000),
    CENT(0.01),
    NICKEL(0.05),
    DIME(0.1),
    QUARTER(0.25);

    private final double denominationValue;

    Denomination(double denominationValue) {
        this.denominationValue = denominationValue;
    }

    public double getDenominationValue() {
        return denominationValue;
    }

    public static Denomination getValue(String denomination) {
        try {
            return Denomination.valueOf(denomination.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnknownDenominationException("Unknown denomination: " + denomination);
        }
    }
}
