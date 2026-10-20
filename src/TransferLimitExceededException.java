public class TransferLimitExceededException extends BankingException {
    private final double amount;
    private final double limit;

    public TransferLimitExceededException(double amount, double limit) {
        super(String.format("Plafond dépassé : %.2f € demandés, plafond de %.2f €", amount, limit));
        this.amount = amount;
        this.limit = limit;
    }

    public double getAmount() { return amount; }
    public double getLimit() { return limit; }
}
