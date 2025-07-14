export const questions = [
  {
    id: 1,
    title: "Two Sum",
    slug: "two-sum",
    difficulty: "Easy",
    submitted: true,
    description: `
Given an array of integers \`nums\` and an integer \`target\`, return the indices of the two numbers such that they add up to \`target\`.

You may assume that each input would have exactly one solution, and you may not use the same element twice.

You can return the answer in any order.

**Example:**
Input: nums = [2,7,11,15], target = 9  
Output: [0,1]
    `,
    starterCode: {
      cpp: `vector<int> twoSum(vector<int>& nums, int target) {\n            // Your code here\n}`,
      java: `public int[] twoSum(int[] nums, int target) {\n            // Your code here\n}`,
      python: `def twoSum(nums, target):\n            # Your code here\n            pass`,
    },
    testCases: [
  {
    input: '{"nums": [2,7,11,15], "target": 9}',
    expectedOutput: "[0,1]"
  }
  ]
  }
];
