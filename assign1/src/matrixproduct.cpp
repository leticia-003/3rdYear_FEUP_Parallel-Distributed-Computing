#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <papi.h>
#include <omp.h>
#include <ctime>
#include <fstream>

using namespace std;

#define SYSTEMTIME clock_t

 
void OnMult(int m_ar, int m_br) 
{
	
	int i, j, k;
    double temp;

	double *pha, *phb, *phc;
	

		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;



	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);



    double start = omp_get_wtime();

	for(i=0; i<m_ar; i++)
	{	for( j=0; j<m_br; j++)
		{	temp = 0;
			for( k=0; k<m_ar; k++)
			{	
				temp += pha[i*m_ar+k] * phb[k*m_br+j];
			}
			phc[i*m_ar+j]=temp;
		}
	}


    double end = omp_get_wtime(); // End timing

    printf("Execution Time: %f seconds\n", end - start);

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

    free(pha);
    free(phb);
    free(phc);
	
	
}

// add code here for line x line matriz multiplication
void OnMultLine(int m_ar, int m_br)
{
    int i, j, k;
    double temp;

    double *pha, *phb, *phc;

    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

    // Initialize matrices
    for (i = 0; i < m_ar; i++)
        for (j = 0; j < m_ar; j++)
            pha[i * m_ar + j] = (double)1.0;

    for (i = 0; i < m_br; i++)
        for (j = 0; j < m_br; j++)
            phb[i * m_br + j] = (double)(i + 1);
    
    for(int i = 0; i < m_ar*m_br; ++i)
        phc[i] = 0;

    // Start timing
    double start = omp_get_wtime();

    // Row-wise multiplication
    for (i = 0; i < m_ar; i++) {
        for (k = 0; k < m_ar; k++) { // Process row i of A
            temp = pha[i * m_ar + k];
            for (j = 0; j < m_br; j++) { // Multiply row i with column j of B
                phc[i * m_ar + j] += temp * phb[k * m_br + j];
            }
        }
    }

    // End timing
    double end = omp_get_wtime(); // End timing

    printf("Execution Time: %f seconds\n", end - start);

    // Display 10 elements of the result matrix to verify correctness
    cout << "Result matrix: " << endl;
    for (i = 0; i < 1; i++) {
        for (j = 0; j < min(10, m_br); j++)
            cout << phc[j] << " ";
    }
    cout << endl;

    free(pha);
    free(phb);
    free(phc);
}

void OnMultLine_OMP1(int m_ar, int m_br)
{
    double *pha, *phb, *phc;
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

    // Initialize matrices
    for (int i = 0; i < m_ar; i++)
        for (int j = 0; j < m_ar; j++)
            pha[i * m_ar + j] = 1.0;

    for (int i = 0; i < m_br; i++)
        for (int j = 0; j < m_br; j++)
            phb[i * m_br + j] = i + 1;
    
    for(int i = 0; i < m_ar*m_br; ++i)
        phc[i] = 0;

    double start = omp_get_wtime(); // Start timing

    // Parallelized version
    #pragma omp parallel for
    for (int i = 0; i < m_ar; i++) {
        for (int k = 0; k < m_ar; k++) {
            double temp = pha[i * m_ar + k]; // Store row element A[i][k]
            for (int j = 0; j < m_br; j++) {
                phc[i * m_ar + j] += temp * phb[k * m_br + j];
            }
        }
    }

    double end = omp_get_wtime(); // End timing

    printf("Execution Time (OMP1): %f seconds\n", end - start);

    cout << "Result matrix: " << endl;
    for (int i = 0; i < 1; i++) {
        for (int j = 0; j < min(10, m_br); j++)
            cout << phc[j] << " ";
    }
    cout << endl;

    free(pha);
    free(phb);
    free(phc);
}

void OnMultLine_OMP2(int m_ar, int m_br)
{
    double *pha, *phb, *phc;
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

    // Initialize matrices
    for (int i = 0; i < m_ar; i++)
        for (int j = 0; j < m_ar; j++)
            pha[i * m_ar + j] = 1.0;

    for (int i = 0; i < m_br; i++)
        for (int j = 0; j < m_br; j++)
            phb[i * m_br + j] = i + 1;

    for(int i = 0; i < m_ar*m_br; ++i)
        phc[i] = 0;

    double start = omp_get_wtime(); // Start timing

    #pragma omp parallel
    {
        for (int i = 0; i < m_ar; i++) {
            for (int k = 0; k < m_ar; k++) {
                double temp = pha[i * m_ar + k]; // Store row element A[i][k]

                #pragma omp for
                for (int j = 0; j < m_br; j++) {
                    phc[i * m_ar + j] += temp * phb[k * m_br + j];
                }
            }
        }
    }

    double end = omp_get_wtime(); // End timing

    printf("Execution Time (OMP2): %f seconds\n", end - start);

    cout << "Result matrix: " << endl;
    for (int i = 0; i < 1; i++) {
        for (int j = 0; j < min(10, m_br); j++)
            cout << phc[j] << " ";
    }
    cout << endl;

    free(pha);
    free(phb);
    free(phc);
}


// add code here for block x block matriz multiplication
void OnMultBlock(int m_ar, int m_br, int bkSize)
{


    double *pha, *phb, *phc;
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phc = (double *)malloc((m_ar * m_ar) * sizeof(double));
    
  
    // Initialize matriz A with 1s
    for(int i = 0; i < m_ar; i++)
        for(int j = 0; j < m_ar; ++j)
            pha[i*m_ar + j] = (double)1.0;
    
    // Initialize matriz B with i+1
    for(int i = 0; i < m_br; i++)
        for(int j = 0; j < m_br; ++j)
            phb[i*m_br + j] = (double)(i+1);

    // Initialize matriz C with 0s
    for(int i = 0; i < m_ar*m_br; ++i)
        phc[i] = 0;
    
    double start = omp_get_wtime();

    int blocksPerRow = (m_ar + bkSize - 1) / bkSize;

    for (int blockY = 0; blockY < blocksPerRow; ++blockY) {
        for (int blockX = 0; blockX < blocksPerRow; ++blockX) {
            int row_start_C = blockY * bkSize;
            int col_start_C = blockX * bkSize;

            int bkSize_Y = min(bkSize, m_ar - row_start_C);  
            int bkSize_X = min(bkSize, m_ar - col_start_C);  

            for (int block = 0; block < blocksPerRow; ++block) {
                int row_start_A = row_start_C;
                int col_start_A = block * bkSize;
                int row_start_B = block * bkSize;
                int col_start_B = col_start_C;

                int bkSize_A = min(bkSize, m_ar - col_start_A);
                int bkSize_B = min(bkSize, m_ar - row_start_B);

                for (int i = 0; i < bkSize_Y; ++i) {
                    for (int n = 0; n < bkSize_A; ++n) {
                        for (int j = 0; j < bkSize_X; ++j) {
                            int indexC = (row_start_C + i) * m_ar + (col_start_C + j);
                            int indexA = (row_start_A + i) * m_ar + (col_start_A + n);
                            int indexB = (row_start_B + n) * m_ar + (col_start_B + j);
                            
                            phc[indexC] += pha[indexA] * phb[indexB];
                        }
                    }
                }
            }
        }
    }

    double end = omp_get_wtime(); // End timing

    printf("Execution Time: %f seconds\n", end - start);

    cout << "Result matrix: " << endl;
    for(int i = 0; i < 1; ++i){
    for(int j = 0; j < min(10,m_br); ++j)
        cout << phc[j] << " ";
    }

    cout << endl;

    free(pha);
    free(phb);
    free(phc);
    
}


void measure_performance(int m_ar, int m_br) {
    double start_serial, end_serial, start_parallel, end_parallel;

    start_serial = omp_get_wtime();
    OnMultLine(m_ar, m_br);
    end_serial = omp_get_wtime();
    
    start_parallel = omp_get_wtime();
    OnMultLine_OMP1(m_ar, m_br);
    end_parallel = omp_get_wtime();

    double serial_time = end_serial - start_serial;
    double parallel_time = end_parallel - start_parallel;
    
    double speedup = serial_time / parallel_time;
    int num_threads = omp_get_max_threads();
    double efficiency = speedup / num_threads;

    printf("Speedup: %f\n", speedup);
    printf("Efficiency: %f\n", efficiency);
}



void handle_error (int retval)
{
  printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
  exit(1);
}

void init_papi() {
  int retval = PAPI_library_init(PAPI_VER_CURRENT);
  if (retval != PAPI_VER_CURRENT && retval < 0) {
    printf("PAPI library version mismatch!\n");
    exit(1);
  }
  if (retval < 0) handle_error(retval);

  std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
            << " MINOR: " << PAPI_VERSION_MINOR(retval)
            << " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
}


int main (int argc, char *argv[])
{

	char c;
	int lin, col, blockSize;
	int op;
	
	int EventSet = PAPI_NULL;
  	long long values[2];
  	int ret;
	

	ret = PAPI_library_init( PAPI_VER_CURRENT );
	if ( ret != PAPI_VER_CURRENT )
		std::cout << "FAIL" << endl;


	ret = PAPI_create_eventset(&EventSet);
		if (ret != PAPI_OK) cout << "ERROR: create eventset" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L1_DCM );
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_DCM" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L2_DCM);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCM" << endl;


	op=1;
	do {
		cout << endl << "1. Multiplication" << endl;
		cout << "2. Line Multiplication" << endl;
		cout << "3. Block Multiplication" << endl;
        cout << "4. Line Multiplication (Parallel 1)" << endl;
        cout << "5. Line Multiplication (Parallel 2)"<< endl;
		cout << "Selection?: ";
		cin >>op;
		if (op == 0)
			break;
		printf("Dimensions: lins=cols ? ");
   		cin >> lin;
   		col = lin;


		// Start counting
		ret = PAPI_start(EventSet);
		if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;

		switch (op){
			case 1: 
				OnMult(lin, col);
				break;
			case 2:
				OnMultLine(lin, col);  
				break;
			case 3:
				cout << "Block Size? ";
				cin >> blockSize;
				OnMultBlock(lin, col, blockSize);  
				break;
            case 4:
                OnMultLine_OMP1(lin, col);
                break;
            case 5:
                OnMultLine_OMP2(lin, col);
                break;
            default:
                op == 0;

		}

  		ret = PAPI_stop(EventSet, values);
  		if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
  		printf("L1 DCM: %lld \n",values[0]);
  		printf("L2 DCM: %lld \n",values[1]);

		ret = PAPI_reset( EventSet );
		if ( ret != PAPI_OK )
			std::cout << "FAIL reset" << endl; 



	}while (op != 0);

	ret = PAPI_remove_event( EventSet, PAPI_L1_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L2_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_destroy_eventset( &EventSet );
	if ( ret != PAPI_OK )
		std::cout << "FAIL destroy" << endl;

}
