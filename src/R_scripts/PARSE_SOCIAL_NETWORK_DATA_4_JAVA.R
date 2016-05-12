####################################
# Analyze facebook data using Java/R
####################################
# author: Lu
# date: April 2016
#
# NOTE: at bekerly13.mat, R died due to lack of memory.  I have 20 GB memory on my computer

# 1. convert the data in MATLAB (MAT) format to text format of "from to" for Java loader

library(R.matlab)

# path to .mat data
path <- c("C:\\Users\\LU\\java_workspace\\SocialNetworks\\data\\facebook100\\facebook100")

# get list of school filenames
schools <- read.table("C:\\Users\\LU\\java_workspace\\SocialNetworks\\data\\facebook100\\facebook100\\school_list.txt", header = TRUE, stringsAsFactors = FALSE)

# outfile captures info on each school:
# node counts
# edge counts
# if matrix is bi-directional
# how many students, faculties, etc
# gender counts
#
outfile = paste(path, "school_info.txt", sep = "\\")

for (i in c(1:nrow(schools))) {

  # read input file
  data <- readMat(paste(path, schools$SCHOOL_NAME[i], sep = "\\"))

  # sparse matrix
  A <- as.matrix(data$A)
  
  cat(paste(schools$SCHOOL_NAME[i], ":\n", sep = ""), 
      file = outfile, 
      fill = FALSE, 
      append = TRUE)
  
  cat(paste("node counts: ", nrow(A), "\n", sep = ""), 
      file = outfile, 
      fill = FALSE, 
      append = TRUE)
  
  edge_info <- as.data.frame(table(A))
  
  if (nrow(edge_info) > 2) {
    cat("data corrupted\n", file = outfile, fill = FALSE, append = TRUE)
  }
  else {
    cat(paste("edge counts: ", edge_info[2,"Freq"], "\n", sep = ""), 
        file = outfile, 
        fill = FALSE, 
        append = TRUE)
    
    cat(paste("is bidirectional: ", isSymmetric(A), "\n", sep = ""), 
        file = outfile, 
        fill = FALSE, 
        append = TRUE)   
    
    # convert the graph from matrix to "from_vertex to_vertex" format for Java program
    graph_input = paste(path, schools$SCHOOL_NAME[i], sep = "\\")
    graph_output = sub(".mat", ".txt", graph_input)
    
    for (j in c(1:nrow(A))) {
      edges <- grep("1", A[j,], value = FALSE)
      for (k in c(1:length(edges))) {
        cat(paste(j, edges[k], "\n", sep = " "), file = graph_output, fill = FALSE, append = TRUE)
      }
    }
    
  }
