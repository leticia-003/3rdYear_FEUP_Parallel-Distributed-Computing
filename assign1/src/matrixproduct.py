import pypapi as papi
import numpy as np
import time

def on_mult(m_ar, m_br):
    # Array initialization
    A = np.ones((m_ar, m_ar))  
    B = np.ones([[i + j for j in range(m_br)] for i in range(m_br)], dtype=float)  
    C = np.zeros((m_ar, m_br))  

    # matrix multiplication
    for i in range(m_ar):
        for j in range(m_br):
            temp = 0
            for k in range(m_ar):
                temp += A[i, k] * B[k, j]
            C[i, j] = temp
    
    print("Result matrix:", C[0, :min(10, m_br)])

def on_mult_line(m_ar, m_br):
   # Array initialization
    A = np.ones((m_ar, m_ar))  
    B = np.array([[i + j for j in range(m_br)] for i in range(m_br)], dtype=float)  
    C = np.zeros((m_ar, m_br))

    #Row wise multiplication
    for i in range(m_ar):
        for k in range(m_ar):
            temp = A[i,k]
            for j in range(m_br):
                C[i,j] += temp * B[k,j]
    
    print("Result matrix:", C[0, :min(10, m_br)])
   

if __name__ == "__main__":
    while True:
        print("\n1. Multiplication")
        print("2. Line Multiplication")
        print("0. Exit")
        op = int(input("Selection?: "))

        if op == 0:
            break
        
        lin = int(input("Dimensions (lins=cols)?: "))
        col = lin

        if op == 1:
            on_mult(lin, col)
        elif op == 2:
            on_mult_line(lin, col)