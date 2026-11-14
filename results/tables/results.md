### All measurements

| Patterns k | Text length n | Algorithm | Avg time (ns) | Operations | Matches | Trie nodes |
|---:|---:|---|---:|---:|---:|---:|
| 10 | 1000 | Aho-Corasick (build + search) | 13,628 | 1306 | 5 | 70 |
| 10 | 1000 | Aho-Corasick (build only) | 15,437 | 86 | - | 70 |
| 10 | 1000 | Aho-Corasick (search only) | 19,788 | 1220 | 5 | - |
| 10 | 1000 | KMP (once per pattern) | 10,844 | 10436 | 5 | - |
| 100 | 1000 | Aho-Corasick (build + search) | 68,681 | 3209 | 50 | 797 |
| 100 | 1000 | Aho-Corasick (build only) | 55,824 | 1480 | - | 797 |
| 100 | 1000 | Aho-Corasick (search only) | 11,971 | 1729 | 50 | - |
| 100 | 1000 | KMP (once per pattern) | 80,021 | 104651 | 50 | - |
| 1000 | 1000 | Aho-Corasick (build + search) | 587,154 | 13301 | 500 | 6502 |
| 1000 | 1000 | Aho-Corasick (build only) | 545,720 | 11951 | - | 6502 |
| 1000 | 1000 | Aho-Corasick (search only) | 46,877 | 1350 | 500 | - |
| 1000 | 1000 | KMP (once per pattern) | 784,610 | 1045810 | 500 | - |
| 10 | 10000 | Aho-Corasick (build + search) | 81,940 | 13549 | 5 | 75 |
| 10 | 10000 | Aho-Corasick (build only) | 4,973 | 98 | - | 75 |
| 10 | 10000 | Aho-Corasick (search only) | 79,766 | 13451 | 5 | - |
| 10 | 10000 | KMP (once per pattern) | 93,534 | 103895 | 5 | - |
| 100 | 10000 | Aho-Corasick (build + search) | 114,900 | 21165 | 50 | 804 |
| 100 | 10000 | Aho-Corasick (build only) | 45,438 | 1531 | - | 804 |
| 100 | 10000 | Aho-Corasick (search only) | 63,093 | 19634 | 50 | - |
| 100 | 10000 | KMP (once per pattern) | 1,321,630 | 1039315 | 50 | - |
| 1000 | 10000 | Aho-Corasick (build + search) | 903,300 | 31121 | 500 | 6907 |
| 1000 | 10000 | Aho-Corasick (build only) | 715,657 | 13514 | - | 6907 |
| 1000 | 10000 | Aho-Corasick (search only) | 187,516 | 17607 | 500 | - |
| 1000 | 10000 | KMP (once per pattern) | 9,512,305 | 10391783 | 500 | - |
| 10 | 100000 | Aho-Corasick (build + search) | 692,524 | 130748 | 5 | 74 |
| 10 | 100000 | Aho-Corasick (build only) | 4,692 | 97 | - | 74 |
| 10 | 100000 | Aho-Corasick (search only) | 679,102 | 130651 | 5 | - |
| 10 | 100000 | KMP (once per pattern) | 998,194 | 1038370 | 5 | - |
| 100 | 100000 | Aho-Corasick (build + search) | 655,522 | 196895 | 50 | 805 |
| 100 | 100000 | Aho-Corasick (build only) | 60,144 | 1534 | - | 805 |
| 100 | 100000 | Aho-Corasick (search only) | 791,245 | 195361 | 50 | - |
| 100 | 100000 | KMP (once per pattern) | 10,923,555 | 10386627 | 50 | - |
| 1000 | 100000 | Aho-Corasick (build + search) | 1,715,882 | 209880 | 501 | 6963 |
| 1000 | 100000 | Aho-Corasick (build only) | 656,740 | 13925 | - | 6963 |
| 1000 | 100000 | Aho-Corasick (search only) | 1,140,679 | 195955 | 501 | - |
| 1000 | 100000 | KMP (once per pattern) | 112,134,652 | 103854037 | 501 | - |

### KMP time divided by Aho-Corasick (build + search) time

Values above 1 mean that Aho-Corasick is faster.

| Patterns k \ Text length n | 1000 | 10000 | 100000 |
|---:|---:|---:|---:|
| 10 | 0.80 | 1.14 | 1.44 |
| 100 | 1.17 | 11.50 | 16.66 |
| 1000 | 1.34 | 10.53 | 65.35 |
