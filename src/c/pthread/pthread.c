#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>
#include <sys/types.h>

void *thread_func(void *param){
  int pid;
  pthread_t thread_id;
  thread_id = pthread_self();
  fprintf(stderr , "thread_func called\n");
  fprintf(stderr , "  thread ID = %ld\n" , thread_id);
  pid = getpid();
  fprintf(stderr , "  2:pid=%d\n" , pid);
}

int hoge() {
  pthread_t thread;
  int pid;
  fprintf(stderr , "---Program start---\n");
  pid = getpid();
  fprintf(stderr , "1:pid = %d\n" , pid);
  pthread_attr_t attr;
  pthread_attr_init(&attr);
  //pthread_attr_setdetachstate(&attr,PTHREAD_CREATE_DETACHED);

  if(pthread_create(&thread , &attr , thread_func , NULL) !=0) {
        perror("pthread_create()");
  }

  pthread_attr_destroy(&attr); 

  fprintf(stderr , "Next line of pthread_create() called. thread ID=%ld\n" , thread);
  //pthread_join(thread , NULL);
}

int main(int argc, char *argv[]){

  int i = 0;
  for (i=0;i<100;i++) {
  	hoge();
  }

  char st[100];
  scanf("%s",st);
  
  return 0;
}

