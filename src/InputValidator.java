import java.util.Scanner;

public class InputValidator {
    public static int getValidChoice(Scanner scanner, int min, int max) {
        while (true) {
            try {
                if (scanner.hasNextInt()) {
                    int choice = scanner. nextInt();
                    if (choice >= min && choice <= max) {
                        return choice;
                    }
                    System. out.print("Please enter a number between " + min + " and " + max + ": ");
                } else {
                    scanner. next();
                    System.out.print("Invalid input. Please enter a number: ");
                }
            } catch (Exception e) {
                scanner.nextLine();
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }
}
