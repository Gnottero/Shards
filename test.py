import math

def rotate_matrix(matrix, angle):
  """
  Rotates a matrix by a specified angle (90 degrees multiples)

  Args:
    matrix: A nested list representing the matrix.
    angle: The rotation angle in degrees (90, 180, 270).

  Returns:
    A nested list representing the rotated matrix.
  """
  
  rows, cols = len(matrix), len(matrix[0])
  
  # Calculate angle in radians and scaling factor based on angle
  angle_rad = math.radians(angle)
  scaling_factor = abs(math.cos(angle_rad))

  # Calculate dimensions of the rotated matrix after scaling
  new_rows = int(rows * scaling_factor)
  new_cols = int(cols * scaling_factor)

  # Initialize rotated matrix with appropriate size
  rotated_matrix = [[None] * new_cols for _ in range(new_rows)]

  # Calculate center of rotation
  center_x = (cols - 1) / 2
  center_y = (rows - 1) / 2

  if angle == 90 or angle == 270:
    for i in range(rows):
      for j in range(cols):
        # Calculate rotated position relative to center
        new_x = (j - center_x) * scaling_factor
        new_y = (i - center_y) * scaling_factor
        
        # Rotate and scale coordinates appropriately
        if angle == 90:
          rotated_x = -new_y + center_x
          rotated_y = new_x + center_y
        else:
          rotated_x = new_y + center_x
          rotated_y = -new_x + center_y
        
        # Check if rotated position is within bounds
        if 0 <= rotated_x < new_cols and 0 <= rotated_y < new_rows:
          rotated_matrix[int(rotated_y)][int(rotated_x)] = matrix[i][j]
  elif angle == 180:
    for i in range(rows):
      for j in range(cols):
        # Similar logic as 90/270, but rotate by 180 degrees
        new_x = (j - center_x) * scaling_factor
        new_y = (i - center_y) * scaling_factor
        rotated_x = -new_x + center_x
        rotated_y = -new_y + center_y
        if 0 <= rotated_x < new_cols and 0 <= rotated_y < new_rows:
          rotated_matrix[int(rotated_y)][int(rotated_x)] = matrix[i][j]
  else:
    raise ValueError("Invalid rotation angle. Must be a multiple of 90 degrees.")

  return rotated_matrix

# Example usage
matrix = [
        [1, 2, 3],
        [43, 52, 13],
        [4, 5, 6],
        [7, 8, 9]
    ]

rotated_matrix = rotate_matrix(matrix, 90)

print("Original matrix:")
for row in matrix:
  print(row)

print("Rotated matrix (90 degrees):")
for row in rotated_matrix:
  print(row)

# Added example for 180-degree rotation
rotated_matrix_180 = rotate_matrix(matrix, 180)

print("Original matrix:")
for row in matrix:
  print(row)

print("Rotated matrix (180 degrees):")
for row in rotated_matrix_180:
  print(row)
