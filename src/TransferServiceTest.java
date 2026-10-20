public class TransferServiceTest {
    private static final TransferService service = new TransferService();
    private static int failures = 0;

    /** Compte qui simule une panne au moment du crédit. */
    static class FailingBankAccount extends BankAccount {
        FailingBankAccount(String number, double balance) { super(number, balance); }
        @Override public void credit(double amount) { throw new IllegalStateException("Panne simulée"); }
    }

    public static void main(String[] args) {
        // 1. Succès
        BankAccount a = new BankAccount("FR001", 2_000), b = new BankAccount("FR002", 500);
        run("1. 500 € depuis 2000 €", () -> service.transfer(a, b, 500), null);
        check("   soldes mis à jour", a.getBalance() == 1_500 && b.getBalance() == 1_000);

        // 2 à 5. Rejets, comptes inchangés
        BankAccount c = new BankAccount("FR001", 2_000), d = new BankAccount("FR002", 500);
        run("2. 3000 € depuis 2000 €", () -> service.transfer(c, d, 3_000), InsufficientFundsException.class);
        BankAccount riche = new BankAccount("FR003", 50_000);
        run("3. 12000 €", () -> service.transfer(riche, d, 12_000), TransferLimitExceededException.class);
        run("4. -100 €", () -> service.transfer(c, d, -100), IllegalArgumentException.class);
        run("5. même compte", () -> service.transfer(c, c, 100), InvalidAccountException.class);
        run("   compte null", () -> service.transfer(c, null, 100), InvalidAccountException.class);
        check("   comptes inchangés après rejets", c.getBalance() == 2_000 && d.getBalance() == 500);

        // 6. « abc » : géré dans Main (NumberFormatException), vérifié ici sur le parsing

        // 7. Panne entre débit et crédit -> compte source restauré, cause conservée
        BankAccount e = new BankAccount("FR001", 2_000);
        BankAccount panne = new FailingBankAccount("FR009", 0);
        try {
            service.transfer(e, panne, 500);
            check("7. panne entre débit et crédit", false);
        } catch (TransferFailedException ex) {
            check("7. panne -> TransferFailedException", true);
            check("   compte source restauré (2000 €)", e.getBalance() == 2_000 && ex.isRolledBack());
            check("   cause d'origine conservée", ex.getCause() instanceof IllegalStateException);
        } catch (BankingException ex) {
            check("7. exception inattendue " + ex, false);
        }

        System.out.println(failures == 0 ? "\nTous les scénarios passent." : "\n" + failures + " échec(s).");
        if (failures > 0) System.exit(1);
    }

    interface Action { void run() throws Exception; }

    private static void run(String name, Action action, Class<? extends Exception> expected) {
        try {
            action.run();
            check(name + " -> succès", expected == null);
        } catch (Exception ex) {
            check(name + " -> " + ex.getClass().getSimpleName(), expected != null && expected.isInstance(ex));
        }
    }

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "[OK]   " : "[FAIL] ") + name);
        if (!ok) failures++;
    }
}
