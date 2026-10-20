
public class TransferFailedException extends BankingException {
    private final boolean rolledBack;

    public TransferFailedException(String message, Throwable cause, boolean rolledBack) {
        super(message, cause);
        this.rolledBack = rolledBack;
    }

    public boolean isRolledBack() { return rolledBack; }
}
