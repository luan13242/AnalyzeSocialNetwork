####################################
# Analyze facebook data using Java/R
####################################
# author: Lu
# date: April 2016

# Version2: save A as a csv file

#library(R.matlab)

# path to .mat data
path <- c("C:\\Users\\LU\\java_workspace\\SocialNetworks\\data\\facebook100\\facebook100")

# get list of school filenames
schools <- read.csv("C:\\Users\\LU\\java_workspace\\SocialNetworks\\data\\facebook100\\facebook100\\school_list.txt", header = TRUE, stringsAsFactors = FALSE)

# outfile captures info on each school:
# node counts
# edge counts
# if matrix is bi-directional
# how many students, faculties, etc
# gender counts
#
outfile = paste(path, "school_info.csv", sep = "\\")
cat(paste("school", "node", "edge", "\n", sep = ","), 
    file = outfile, 
    fill = FALSE, 
    append = TRUE)

for (i in c(1:nrow(schools))) {
  
  # read input file
  data <- readMat(paste(path, schools$SCHOOL_NAME[i], sep = "\\"))
  
  # sparse matrix
  A <- as.matrix(data$A)
  
  graph_input = paste(path, schools$SCHOOL_NAME[i], sep = "\\")
  graph_output = sub(".mat", ".csv", graph_input)
  write.csv(A, file = graph_output, row.names = FALSE, col.names = FALSE)
  
  edge_info <- as.data.frame(table(A))
  
  if (nrow(edge_info) > 2) {
    print(schools$SCHOOL_NAME[i])
    print("data corrupted")
    break
  }
  
  cat(paste(schools$SCHOOL_NAME[i], nrow(A), edge_info[2,"Freq"], "\n", sep = ","), 
        file = outfile, 
        fill = FALSE, 
        append = TRUE)

}
