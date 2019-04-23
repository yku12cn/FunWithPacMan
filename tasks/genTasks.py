
import numpy

def main():

  # Editable parameters
  learner = 'independent'
  length = 50

  targetTaskFile = 'targetTask'
  targetFile = 'independent_1'


  targetPolicy = 'subTask1'

  # Task set parameters!

  ghostType = range(4)
  numGhosts = range(5)
  ghostSpeedReduction = range(1,3)
  mazeNum = range(6)
  edibleTime = [200, 2000] #range(100, 500, 100) + [2000]
  completionBonus = [0, 100]

  repeats = range(1,6)

  for gt in ghostType:
    for ng in numGhosts:
      for gsr in ghostSpeedReduction:
        for m in mazeNum:
          for et in edibleTime:
            for cb in completionBonus:
              
              taskFile = 'ghostType%dNumGhosts%dSpeedReduction%dMaze%dEdibleTime%dBonus%d' % (gt, ng, gsr, m, et, cb)

              f = open(taskFile + '.task', 'w')
              
              # Task specific parameters
              f.write('MAZE_NUM:%d\n'%m)
              f.write('GHOST_SPEED_REDUCTION:%d\n'%gsr)
              f.write('NUM_GHOSTS:%d\n'%ng)
              f.write('GHOST_TYPE:%d\n'%gt)
              f.write('EDIBLE_TIME:%d\n'%et)
              f.write('BOARD_COMPLETION_BONUS:%d\n'%cb)

              # Common parameters
              f.write('LEARNER:%s\n'%learner)
              f.write('LENGTH:%d\n'%length)
              f.write('SOURCE_TASK:%s\n'%taskFile)              
              f.write('SAVE_DIR:%s\n'%taskFile)

              f.close()
              
              # Condor output
              for r in repeats:
#                print 'Arguments = pacman.Experiments tasks/' + taskFile + '.task tasks/' + targetTaskFile + ' ' + targetFile + '/avg_curve ' + str(r)
#                print 'Queue 1'
#                print

                print 'Arguments = pacman.Experiments tasks/' + taskFile + '.task ' + targetPolicy + '/policy' + str(r-1) + ' ' + str(r) 
                print 'Queue 1'
                print


#              print 'Saving', taskFile, '.task'
#
#  print 'Finished.'



if __name__=="__main__":
  main()

