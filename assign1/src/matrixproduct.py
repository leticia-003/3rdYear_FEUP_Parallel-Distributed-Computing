import time

def on_mult(m_ar, m_br):
    # Array initialization
    A = [[1.0 for _ in range(m_ar)] for _ in range(m_ar)]
    B = [[float(i + j) for j in range(m_br)] for i in range(m_br)]
    C = [[0.0 for _ in range(m_br)] for _ in range(m_ar)]
    
    start_time = time.perf_counter()

    # matrix multiplication
    for i in range(m_ar):
        for j in range(m_br):
            temp = 0
            for k in range(m_ar):
                temp += A[i, k] * B[k, j]
            C[i, j] = temp
    
    #stop counters
    end_time = time.perf_counter()
    
    #results
    print(f"Time: {end_time - start_time:.6f} seconds")
    print("Result matrix:", C[0, :min(10, m_br)])



def on_mult_line(m_ar, m_br):
    # Array initialization
    A = [[1.0 for _ in range(m_ar)] for _ in range(m_ar)]
    B = [[float(i + j) for j in range(m_br)] for i in range(m_br)]
    C = [[0.0 for _ in range(m_br)] for _ in range(m_ar)] 

    
    start_time = time.perf_counter()

    #multline

    for i in range(m_ar) :
        for k in range(m_ar) :
            temp = A[i, k]
            for j in range(m_br) :
                C[i,j] += temp * B[k, j]

    #stop counters
    end_time = time.perf_counter()
    
    #results
    print(f"Time: {end_time - start_time:.6f} seconds")
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