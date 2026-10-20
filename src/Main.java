import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        BankAccount compteA = new BankAccount("FR001", 2_000);
        BankAccount compteB = new BankAccount("FR002", 500);
        Map<String, BankAccount> comptes = Map.of("FR001", compteA, "FR002", compteB);
        TransferService service = new TransferService();

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Comptes : " + compteA + " | " + compteB);
            try {
                System.out.print("Compte source (FR001) : ");
                BankAccount source = comptes.get(scanner.nextLine().trim());
                System.out.print("Compte destinataire (FR002) : ");
                BankAccount destination = comptes.get(scanner.nextLine().trim());
                System.out.print("Montant du virement : ");
                double montant = Double.parseDouble(scanner.nextLine().trim().replace(',', '.'));

                service.transfer(source, destination, montant);
                System.out.println("Virement effectué. " + compteA + " | " + compteB);

            } catch (NumberFormatException e) {
                System.out.println("Saisie invalide : veuillez entrer un nombre.");
            } catch (IllegalArgumentException e) {
                System.out.println("Virement rejeté : " + e.getMessage());
            } catch (InsufficientFundsException e) {
                System.out.printf("Fonds insuffisants : il manque %.2f €.%n", e.getMissingAmount());
            } catch (TransferLimitExceededException e) {
                System.out.printf("Plafond dépassé : maximum %.2f € par virement.%n", e.getLimit());
            } catch (InvalidAccountException e) {
                System.out.println("Compte invalide : " + e.getMessage());
            } catch (TransferFailedException e) {
                System.out.println("Erreur technique : " + e.getMessage() + " (cause : " + e.getCause() + ")");
            } finally {
                System.out.println("Traitement de la transaction terminé.");
            }
        }
    }
}
