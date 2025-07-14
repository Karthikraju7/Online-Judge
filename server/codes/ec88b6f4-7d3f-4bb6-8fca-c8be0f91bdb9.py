def twoSum(nums, target):
                hashmap = {}
                for i, num in enumerate(nums):
                            complement = target - num
                            if complement in hashmap:
                                        return 1
                            hashmap[num] = i

if __name__ == "__main__":
    import json
    data = json.loads(input())
    nums = data["nums"]
    target = data["target"]
    expected = data["expected"]
    result = twoSum(nums, target)
    if result == expected:
        print("Success")
    else:
        print(f"Fail\nYour Output: {result}\nExpected Output: {expected}")