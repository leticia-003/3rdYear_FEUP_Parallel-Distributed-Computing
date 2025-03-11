import pypapi as papi
import numpy as np
import time

event_set = 0

def on_mult(m_ar, m_br):
    # Array initialization
    A = np.ones((m_ar, m_ar))  
    B = np.ones([[i + j for j in range(m_br)] for i in range(m_br)], dtype=float)  
    C = np.zeros((m_ar, m_br))  

    #start counters
    try:
        papi.papi_low.start(event_set)
    except:
        "Error starting event set.\n"
    
    start_time = time.time()

    # matrix multiplication
    for i in range(m_ar):
        for j in range(m_br):
            temp = 0
            for k in range(m_ar):
                temp += A[i, k] * B[k, j]
            C[i, j] = temp
    
    #stop counters
    end_time = time.time()
    try:
        values = papi.papi_low.stop(event_set)
    except:
        "Error stopping event set.\n"
    
    #results
    print(f"Time: {end_time - start_time:.3f} seconds")
    print(f"L1 DCM: {values[0]}")
    print(f"L2 DCM: {values[1]}")
    print("Result matrix:", C[0, :min(10, m_br)])

    try:
        papi.papi_low.reset(event_set)
    except:
        print("Error resetting event set.\n")

def on_mult_line(m_ar, m_br):
    #initialization

    #start counters
    try:
        papi.papi_low.start(event_set)
    except:
        "Error starting event set.\n"
    
    start_time = time.time()
    #multline

    #stop counters
    end_time = time.time()
    try:
        values = papi.papi_low.stop(event_set)
    except:
        "Error stopping event set.\n"
    
    #results
    print(f"Time: {end_time - start_time:.3f} seconds")
    print(f"L1 DCM: {values[0]}")
    print(f"L2 DCM: {values[1]}")

    try:
        papi.papi_low.reset(event_set)
    except:
        print("Error resetting event set.\n")
    
   

if __name__ == "__main__":
    #initialize papi
    try:
        papi.papi_low.library_init(papi.consts.PAPI_VER_CURRENT)
    except:
        print("Error initializing pypapi.\n")
    
    # initialize event set
    try:
        event_set = papi.papi_low.create_eventset()
    except:
        print("Error creating event set.\n")
    
    # add events

    try:
        papi.papi_low.add_events(event_set, [papi.events.PAPI_L1_DCM, papi.events.PAPI_L2_DCM])
    except:
        print("Error adding events. Check /proc/sys/kernel/perf_event_paranoid.\n")


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
    
    try:
        papi.papi_low.remove_events(event_set, [papi.events.PAPI_L1_DCM, papi.events.PAPI_L2_DCM])
    except:
        print("Error removing events. \n")

    try: 
        papi.papi_low.destroy_eventset(event_set)
    except:
        print("Error destroying event set.\n")