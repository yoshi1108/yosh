#include <stdio.h>
#include <stdlib.h>
#include "control.h"
#include "queue.h"
#include "dbug.h"

#define NUM_WORKERS 4

struct work_queue {
  data_control control;
  queue work;
} wq;

/* I added a job number to the work node.  Normally, the work node
   would contain additional data that needed to be processed. */
typedef struct work_node {
  struct node *next;
  int jobnum;
} wnode;

void *threadfunc(void *myarg) {

  wnode *mywork;
  pthread_mutex_lock(&wq.control.mutex);
    
  while (wq.control.active) {
    while (wq.work.head==NULL && wq.control.active) {
      pthread_cond_wait(&wq.control.cond, &wq.control.mutex);
    }
    if (!wq.control.active) 
      break;
    //we got something!
    mywork=(wnode *) queue_get(&wq.work);
    pthread_mutex_unlock(&wq.control.mutex);
    //perform processing...
    printf("processing job %d\n",mywork->jobnum);
    free(mywork);
    pthread_mutex_lock(&wq.control.mutex);
  }

  pthread_mutex_unlock(&wq.control.mutex);
  return NULL;
}

int numthreads;
int create_threads(void) {
  int x;
  pthread_t pt;
  for (x=0; x<NUM_WORKERS; x++) {
    if (pthread_create(&pt, NULL, threadfunc, NULL)) {
      return 1;
	}
    printf("created thread %d\n",x);
    numthreads++;
  }
  return 0;
}

void initialize_structs(void) {
  numthreads=0;
  if (control_init(&wq.control))
    dabort();
  queue_init(&wq.work);
  queue_init(&wq.work);
  control_activate(&wq.control);
}

int main(void) {

  int x;
  wnode *mywork;

  initialize_structs();

  /* CREATION */
  if (create_threads()) {
    printf("Error starting threads... cleaning up.\n");
    dabort();
  }

  pthread_mutex_lock(&wq.control.mutex);
  for (x=0; x<10; x++) {
    mywork=malloc(sizeof(wnode));
    if (!mywork) {
      printf("ouch! can't malloc!\n");
      break;
    }
    mywork->jobnum=x;
    queue_put(&wq.work,(node *) mywork);
  }
  pthread_mutex_unlock(&wq.control.mutex);
  pthread_cond_broadcast(&wq.control.cond);

  printf("sleeping...\n");
  sleep(1);
  printf("deactivating work queue...\n");
}
