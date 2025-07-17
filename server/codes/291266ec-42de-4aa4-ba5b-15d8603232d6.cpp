#include <iostream>
#include <vector>
#include <sstream>
#include <string>
#include <unordered_map>
#include <algorithm>
using namespace std;

vector<int> twoSum(vector<int>& nums, int target) {
    unordered_map<int,int> map;
                int n = nums.size();
                for(int i = 0; i <n; ++i ){
                            if(map.count(target-nums[i])){
                                        return {i, map[target-nums[i]]};
                            }
                            map[nums[i]] = i;
                }           
                return {};
}

int main() {
    string line;
    getline(cin, line);
    vector<int> nums, expected;
    int target;

    size_t numsPos = line.find("nums");
    size_t targetPos = line.find("target");
    size_t expectedPos = line.find("expected");

    size_t start = line.find("[", numsPos);
    size_t end = line.find("]", start);
    string numsStr = line.substr(start + 1, end - start - 1);
    stringstream ss(numsStr);
    string val;
    while (getline(ss, val, ',')) {
        nums.push_back(stoi(val));
    }

    start = line.find(":", targetPos);
    string targetStr = line.substr(start + 1);
    targetStr = targetStr.substr(0, targetStr.find(","));
    target = stoi(targetStr);

    start = line.find("[", expectedPos);
    end = line.find("]", start);
    string expectedStr = line.substr(start + 1, end - start - 1);
    stringstream ess(expectedStr);
    while (getline(ess, val, ',')) {
        expected.push_back(stoi(val));
    }

    vector<int> result = twoSum(nums, target);
    if (result == expected) {
        cout << "Success" << endl;
    } else {
        cout << "Fail" << endl;
        cout << "Your Output: [";
        for (int i = 0; i < result.size(); i++) {
            cout << result[i];
            if (i != result.size() - 1) cout << ",";
        }
        cout << "]" << endl;
        cout << "Expected Output: [";
        for (int i = 0; i < expected.size(); i++) {
            cout << expected[i];
            if (i != expected.size() - 1) cout << ",";
        }
        cout << "]" << endl;
    }
    return 0;
}