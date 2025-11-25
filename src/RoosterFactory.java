public class RoosterFactory {

    public static Rooster createRooster(int choice) {
        switch (choice) {
            case 1:
                return new Rooster.ManokNaPuti("Manok na Puti");
            case 2:
                return new Rooster.ManokNaItim("Manok na Itim");
            case 3:
                return new Rooster.ManokNaBato("Manok na Bato");
            default:
                System.out.println("Invalid choice. Defaulting to Manok na Puti.");
                return new Rooster.ManokNaPuti("Default Puti");
        }
    }
}
  
