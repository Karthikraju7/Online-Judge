def twoSum(nums, target):
             for i in range(len(nums)):
                        for j in range(i + 1, len(nums)):
                                    if nums[i] + nums[j] == target:
                                                return [i, j]

if __name__ == "__main__":
    import json
    data = json.loads(input())
    nums = data["nums"]
    target = data["target"]
    expected = data["expected"]

    if isinstance(expected, str):
        expected = json.loads(expected)

    result = twoSum(nums, target)
    if result == expected:
        print("Success")
    else:
        print(f"Fail\nYour Output: {result}\nExpected Output: {expected}")