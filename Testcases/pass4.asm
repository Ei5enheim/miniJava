  0         PUSH         1
  1         LOADL        0
  2         STORE        0[SB]
  3         LOADL        -1
  4         LOADL        3
  5         PUSH         3
  6         LOADL        -1
  7         LOADL        0
  8         PUSH         1
  9         LOADL        0
 10         STORE        8[SB]
 11         LOADL        -1
 12         LOADL        0
 13         LOADL        -1
 14         LOADL        1
 15         PUSH         1
 16         LOADL        19
 17         STORE        3[SB]
 18         JUMP         L12
 19  L10:   LOADA        11[SB]
 20         LOADL        0
 21         CALL         newobj  
 22         STORE        8[SB]
 23         PUSH         1
 24         LOADA        1[SB]
 25         LOADL        1
 26         CALL         newobj  
 27         STORE        3[LB]
 28         LOADL        0
 29         LOAD         3[LB]
 30         CALLD        0
 31         JUMPIF (0)   L11
 32         LOADL        4
 33         LOAD         8[SB]
 34         CALLD        0
 35  L11:   PUSH         1
 36         LOADL        2
 37         CALL         newarr  
 38         STORE        4[LB]
 39         LOAD         4[LB]
 40         LOADL        0
 41         LOAD         3[LB]
 42         CALLD        0
 43         CALL         arrayupd
 44         RETURN (0)   0
 45  L12:   LOADL        48
 46         STORE        4[SB]
 47         JUMP         L15
 48  L13:   PUSH         1
 49         LOADL        0
 50         STORE        3[LB]
 51         LOAD         -1[LB]
 52         LOADL        0
 53         CALL         eq      
 54         JUMPIF (0)   L14
 55         LOADL        1
 56         STORE        3[LB]
 57  L14:   LOAD         3[LB]
 58         RETURN (1)   1
 59  L15:   LOADL        62
 60         STORE        5[SB]
 61         JUMP         L17
 62  L16:   LOADL        0
 63         RETURN (1)   0
 64  L17:   LOADL        67
 65         STORE        13[SB]
 66         JUMP         L19
 67  L18:   LOAD         -1[LB]
 68         CALL         putint  
 69         RETURN (0)   1
 70  L19:   LOADL        -1
 71         CALL         L10
 72         HALT   (0)   
