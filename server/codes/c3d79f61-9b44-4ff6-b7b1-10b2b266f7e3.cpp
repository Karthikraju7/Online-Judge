#include <iostream>
#include <vector>
using namespace std;

int main() {
    vector<int> nums;
    int x;
    while (cin.peek() != '\n') {
        cin >> x;
        nums.push_back(x);
    }
    int target;
    cin >> target;

    for (int i = 0; i < nums.size(); ++i) {
        for (int j = i + 1; j < nums.size(); ++j) {
            if (nums[i] + nums[j] == target) {
                cout << "[" << i << "," << j << "]" << endl;
                return 0;
            }
        }
    }

    cout << "[]" << endl;
    return 0;
}