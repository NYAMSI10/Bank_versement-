public class InsufficientFundsException extends BankingException {
    private final String accountNumber;
    private final double balance;
    private final double requestedAmount;

    public InsufficientFundsException(String accountNumber, double balance, double requestedAmount) {
        super(String.format("Fonds insuffisants sur le compte %s : solde %.2f €, montant demandé %.2f €",
                accountNumber, balance, requestedAmount));
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.requestedAmount = requestedAmount;
    }

    public String getAccountNumber() { return accountNumber; }
    public double getBalance() { return balance; }
    public double getRequestedAmount() { return requestedAmount; }
    public double getMissingAmount() { return requestedAmount - balance; }
}
