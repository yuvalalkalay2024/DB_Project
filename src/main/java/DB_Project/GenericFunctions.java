package DB_Project;
import java.util.InputMismatchException;
import java.util.Scanner;

public abstract class GenericFunctions {
    static Scanner s = new Scanner(System.in);
    static int ExceptionCheckDomain(String Message, int max, int min) {
        while (true) {
            try {
                System.out.print(Message);
                int object = s.nextInt();
                s.nextLine();
                if (object >= min && object <= max) {
                    return object;
                } else {
                    throw new Exception("Invalid input. the number must be between " + min + " to " + max);
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                s.nextLine();
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
    static float ExceptionCheckPositive(String Message) {
        while (true) {
            try {
                System.out.print(Message);
                float object = s.nextFloat();
                s.nextLine();
                if (object > 0) {
                    return object;
                } else {
                    throw new Exception("Invalid input. The Input must be a positive number!");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                s.nextLine();
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
    static String ExceptionCheckYesOrNO(String Message) {
        while (true) {
            try {
                System.out.print(Message);
                String answer = s.next();
                s.nextLine();
                if (answer.equals("no") || answer.equals("yes")) {
                    return answer;
                } else {
                    throw new Exception("Invalid input. type 'yes' or 'no' please");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}