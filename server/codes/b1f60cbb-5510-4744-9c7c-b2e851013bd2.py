def twoSum(nums, target):
                num_map = {}  # value -> index
                for i, num in enumerate(nums):
                            complement = target - num
                            if complement in num_map:
                                        return [num_map[complement], i]
                            num_map[num] = i

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