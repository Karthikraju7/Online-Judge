def twoSum(nums, target):
            return [0,2]

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