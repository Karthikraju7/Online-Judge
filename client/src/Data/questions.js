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
    starterCode: `function twoSum(nums, target) {\n  // Your code here\n}`,
    testCases: [
      {
        input: "nums = [2,7,11,15], target = 9",
        expectedOutput: "[0,1]"
      },
      {
        input: "nums = [3,2,4], target = 6",
        expectedOutput: "[1,2]"
      }
    ]
  },
  {
    id: 2,
    title: "Reverse Linked List",
    slug: "reverse-linked-list",
    difficulty: "Medium",
    submitted: false,
    description: `
Given the head of a singly linked list, reverse the list, and return the head of the reversed list.

**Example:**

Input: head = [1,2,3,4,5]  
Output: [5,4,3,2,1]
    `,
    starterCode: `function reverseList(head) {\n  // Your code here\n}`,
    testCases: [
      {
        input: "head = [1,2,3,4,5]",
        expectedOutput: "[5,4,3,2,1]"
      },
      {
        input: "head = [1,2]",
        expectedOutput: "[2,1]"
      }
    ]
  }
];
