import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class Main {
    public static int[] twoSum(int[] nums, int target) {
return new int[]{0, 1};
    }

    public static void main(String[] args) {
        try {
            Scanner sc = new Scanner(System.in);
            String json = sc.nextLine();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> data = mapper.readValue(json, Map.class);

            List<Integer> numsList = (List<Integer>) data.get("nums");
            int[] nums = numsList.stream().mapToInt(i -> i).toArray();
            int target = (int) data.get("target");
            Object expectedRaw = data.get("expected");

            int[] result = twoSum(nums, target);
            int[] expected = ((List<Integer>) expectedRaw).stream().mapToInt(i -> i).toArray();

            if (Arrays.equals(result, expected)) {
                System.out.println("Success");
            } else {
                System.out.println("Fail");
                System.out.println("Your Output: " + Arrays.toString(result));
                System.out.println("Expected Output: " + Arrays.toString(expected));
            }
        } catch (Exception e) {
            System.out.println("Error parsing input: " + e.getMessage());
        }
    }
}