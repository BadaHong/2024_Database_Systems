import csv
import sys

class Pair: # key-value pair
    def __init__(self, key, value, left_child=None):
        self.key = key
        self.value = value
        self.left_child = left_child # left child pointer

class BPlusTreeNode: # used for both non-leaf and leaf nodes
    def __init__(self, is_leaf=False):
        self.m = 0 # number of keys
        self.p = [] # array of Pair
        self.r = None # pointer to rightmost child node or right sibling node
        self.is_leaf = is_leaf
        self.parent = None
    
    def add_pair(self, key, value, node=None): # add a pair at the end
        self.p.append(Pair(key, value, node))
        self.m += 1

    def add_at_index(self, key, value, index): # add a pair at an index
        self.p.insert(index, Pair(key, value))
        self.m += 1

    def remove_pair(self, index): # remove a pair at an index
        self.p.pop(index)
        self.m -= 1

    def set_left_child(self, index, node):
        if index == self.m:
            self.r = node # it's rightmost child node
        else:
            self.p[index].left_child = node
    
    def get_left_child(self, index):
        if index == self.m:
            return self.r # it's rightmost child node
        return self.p[index].left_child

class BPlusTree: # B+ Tree
    def __init__(self, degree=0):
        self.root = BPlusTreeNode()
        self.degree = degree # degree from input
    
    # Load
    def load_tree(self, index_file):
        with open(index_file, 'r') as file: # open file in read mode
            self.degree = int(file.readline().strip()) # set degree from first line
            nodes_data = file.readlines() # store data
            
            if not nodes_data: # first initialization - when tree is empty
                self.root = BPlusTreeNode(is_leaf=True) # root is leaf
                return
            
            def build_node(data_line): # creating node
                node_info = data_line.strip().split() # read data in line
                is_leaf = bool(int(node_info[0]))  # 0 or 1 for is_leaf
                m = int(node_info[1])  # number of keys in a node

                # create node
                node = BPlusTreeNode(is_leaf=is_leaf)
                node.m = m
                pairs = node_info[2:]  # key-value pairs
                for pair in pairs:
                    key, value = map(int, pair.split(','))
                    node.p.append(Pair(key, value))  # add key-value pair
                return node
            
            def build_tree_from_data(index, last_leaf=None): # recursive function to make tree
                if index >= len(nodes_data): # return when no left data anymore
                    return None, index, last_leaf

                node = build_node(nodes_data[index]) # build a node
                index += 1 # move the index to next line

                if not node.is_leaf: # if nonleaf, recursively build its children
                    for i in range(node.m + 1):
                        child, index, last_leaf = build_tree_from_data(index, last_leaf) # build child
                        if child:
                            child.parent = node
                            node.set_left_child(i, child) # set its child in appropriate index
                    node.r = node.get_left_child(node.m) # rightmost child for nonleaf node
                
                if node.is_leaf: # right sibling node for leaf node
                    if last_leaf is not None:
                        last_leaf.r = node
                    last_leaf = node
                return node, index, last_leaf

            self.root, _, _ = build_tree_from_data(0) # building tree from the root
    
    # Save
    def save_tree(self, index_file): # save tree in index_file
        with open(index_file, 'w') as file: # open file in write mode
            file.write(f"{self.degree}\n")
            self.save_node(self.root, file) # save node recursively

    def save_node(self, node, index_file):
        if node is None or node.m == 0:
            return

        # node information
        index_file.write(f"{'1' if node.is_leaf else '0'} {node.m} ") # is_leaf & number of keys info
        for pair in node.p:
            index_file.write(f"{pair.key},{pair.value} ") #pairs
        index_file.write("\n")

        if not node.is_leaf:
            for i in range(node.m + 1):
                child = node.get_left_child(i)
                if child:
                    self.save_node(child, index_file) # recursively save children for nonleaf nodes

    # Insertion
    def insertion(self, key, value):
        leaf = self.find_leaf_node(key) # find appropriate leaf node
        
        # insert into leaf
        i = 0
        while i < leaf.m and key > leaf.p[i].key: # find index
            i += 1
        
        if i < leaf.m and leaf.p[i].key == key: # error for duplicated key
            print(f"Key {key} already exists")
            return 
        
        leaf.add_at_index(key, value, i) # add pair at index
        
        if leaf.m > self.degree - 1: # if overflow, split it
            self.split_leaf_node(leaf)

    def find_leaf_node(self, key):
        node = self.root
        while node and not node.is_leaf:
            i = 0
            while i < node.m and key >= node.p[i].key:
                i += 1
            node = node.get_left_child(i)
        return node

    def split_leaf_node(self, leaf):
        new_leaf = BPlusTreeNode(is_leaf=True)

        middle = self.degree // 2 # find middle index for splitting

        new_leaf.p = leaf.p[middle:] # move second half in new leaf node
        new_leaf.m = len(new_leaf.p)

        leaf.p = leaf.p[:middle] # first half in original leaf node
        leaf.m = len(leaf.p)

        new_leaf.r = leaf.r # set right sibling
        leaf.r = new_leaf
        
        self.propagate_split(leaf, new_leaf, new_leaf.p[0].key, new_leaf.p[0].value) # propagate spliting to parent

    def propagate_split(self, old_node, new_node, new_key, new_value): # propagate the splitted key to parent node
        if old_node == self.root: # if it is root, create new root
            new_root = BPlusTreeNode(is_leaf=False)
            new_root.add_pair(new_key, new_value, old_node)
            new_root.r = new_node
            self.root = new_root
            old_node.parent = new_root
            new_node.parent = new_root
        else:
            parent = old_node.parent
            i = 0
            while i < parent.m and new_key > parent.p[i].key: # find index to be inserted
                i += 1
            parent.add_at_index(new_key, new_value, i)
            parent.set_left_child(i, old_node) # set left child with old node
            parent.set_left_child(i + 1, new_node) # set right child with new node
            
            new_node.parent = parent # set parent

            if parent.m > self.degree - 1: # if parent node overflows, split it
                middle_key, middle_value, new_parent = self.split_nonleaf_node(parent)
                self.propagate_split(parent, new_parent, middle_key, middle_value)
    
    def split_nonleaf_node(self, node):
        new_node = BPlusTreeNode(is_leaf=False)
        
        middle = node.m // 2 # middle index for splitting
        middle_key = node.p[middle].key
        middle_value = node.p[middle].value

        new_node.p = node.p[middle + 1:] # second half in new nonleaf node
        new_node.m = len(new_node.p)
        new_node.r = node.r
        
        temp = node.get_left_child(middle) # store middle index key's left child

        node.p = node.p[:middle] # first half in original nonleaf node
        node.m = len(node.p)
        node.r = temp # middle index key's left child becomes node's rightmost left child
        
        for pair in new_node.p: # update parent of new_node's children
            if pair.left_child:
                pair.left_child.parent = new_node # set parent
        if new_node.r:
            new_node.r.parent = new_node # set parent

        return middle_key, middle_value, new_node
    
    # Deletion
    def deletion(self, key):
        leaf = self.find_leaf_node(key) # find appropriate leaf node
        
        i = 0
        while i < leaf.m:
            if leaf.p[i].key == key: # find index where key exists
                break
            i += 1
        if i == leaf.m:
            print(f"Key {key} not found in the tree") # error for not existing key
            return None

        self.delete_from_leaf(key, leaf, i) # delete key from leaf node
        return key
    
    def delete_from_leaf(self, key, leaf, index):
        min = (self.degree - 1) // 2 # minimum key number in node
        
        leaf.remove_pair(index) # remove key from leaf
        
        if 0 < leaf.m and key < leaf.p[0].key:
            self.update_parent(leaf, leaf.p[0].key, leaf.p[0].value, key) # update parents' key if it was key value with smallest key value in leaf node
        
        if leaf.m >= min: # leaf is not underflowed
            if index == 0 and leaf.parent:
                self.update_parent_key(leaf) # update parent's key and return
            return

        if leaf == self.root: # if root, it's okay to be underflowed
            if leaf.m == 0: # if root is empty, set root as None
                self.root = None
            return
        
        parent = leaf.parent
        
        parent_index = self.find_parent_index(leaf)
        left_sibling = parent.get_left_child(parent_index - 1) if parent_index > 0 else None # set left sibling
        right_sibling = parent.get_left_child(parent_index + 1) if parent_index < parent.m else parent.r # set right sibling
        
        if left_sibling and left_sibling.m > min: # borrow from left
            leaf_first = leaf.p[0].key if leaf.m != 0 else key # store smallest key in leaf
            leaf.add_at_index(left_sibling.p[-1].key, left_sibling.p[-1].value, 0) # add left's last key to leaf
            left_sibling.remove_pair(-1) # remove key from left sibling
            
            # update parent's key for each node
            self.update_parent_key(left_sibling)
            self.update_parent_key(leaf)
            
            if leaf_first:
                self.update_parent(parent, leaf.p[0].key, leaf.p[0].value, leaf_first) # update parents' key if they had deleted key value
        elif right_sibling and right_sibling.m > min: # borrow from right
            leaf_first = leaf.p[0].key if leaf.m != 0 else key # store smallest key in leaf
            leaf.add_pair(right_sibling.p[0].key, right_sibling.p[0].value) # add right's first key to leaf
            right_sibling.remove_pair(0) # remove key from right sibling
            
            # update parent's key for each node
            self.update_parent_key(leaf)
            self.update_parent_key(right_sibling)
            
            if leaf_first:
                self.update_parent(parent, leaf.p[0].key, leaf.p[0].value, leaf_first) # update parents' key if they had deleted key value
        else: # merge
            if left_sibling: # with left sibling
                self.merge_leaf(left_sibling, leaf, parent, parent_index - 1) # separator key is one before parent_index
            elif right_sibling: # with right sibling
                self.merge_leaf(leaf, right_sibling, parent, parent_index)
                
        self.update_parent_key(leaf) # update parent's key after all process
            
    def merge_leaf(self, left, right, parent, separator):
        right_first = right.p[0].key if right.m != 0 else None # store smallest key in right
        left.p.extend(right.p) # merge
        left.m = len(left.p)
        left.r = right.r # set right sibling
        
        if right_first and parent.p[0].left_child == left:
            self.update_parent(parent, left.p[0].key, left.p[0].value, right_first) # update parents' key if they had right's first key value
        
        if separator + 1 < parent.m: # except two last index
            parent.p[separator + 1].left_child = left # set left child in separator key's right side
            if separator != 0:
                parent.p[separator].key = left.p[0].key
                parent.p[separator].value = left.p[0].value
            parent.remove_pair(separator) # delete separator from parent
        elif separator + 1 == parent.m: # if second last index
            parent.r = left # set rightmost child node with left
            parent.remove_pair(separator) # delete separator from parent
            if parent.m != 0:
                parent.p[-1].key = left.p[0].key
                parent.p[-1].value = left.p[0].value
        elif separator == parent.m: # if last index
            parent.r = left # set rightmost child node with left
            parent.remove_pair(separator) # delete separator from parent
            if parent.m != 0:
                parent.p[-1].key = left.p[0].key
                parent.p[-1].value = left.p[0].value

        if parent.m < (self.degree - 1) // 2: # when underflow
            if parent == self.root: # if root, it's okay to be underflowed
                if parent.m == 0: # if root is empty
                    self.root = left # set left as new root
                    self.root.parent = None
            else:
                self.handle_internal_underflow(parent) # handle underflow
                
        self.update_parent_key(left) # update parent's key of left
    
    def handle_internal_underflow(self, node):
        min = (self.degree - 1) // 2 # minimum key number in node
        
        parent = node.parent
        parent_index = self.find_parent_index(node)
        left_sibling = parent.get_left_child(parent_index - 1) if parent_index > 0 else None # set left sibling
        right_sibling = parent.get_left_child(parent_index + 1) if parent_index < parent.m else parent.r # set right sibling
        
        if left_sibling and left_sibling.m > min: # borrow from left
            node.add_at_index(parent.p[parent_index - 1].key, parent.p[parent_index - 1].value, 0) # add parent's separator key in front
            node.p[0].left_child = left_sibling.r # move left_sibling's rightmost child to the node's first child
            node.p[0].left_child.parent = node # its parent is node now
            left_sibling.r = left_sibling.p[-1].left_child # left sibling's rightmost child is now last left child
            
            # update parent's key
            parent.p[parent_index-1].key = left_sibling.p[-1].key
            parent.p[parent_index-1].value = left_sibling.p[-1].value
            
            left_sibling.remove_pair(-1) # remove key from left sibling
        elif right_sibling and right_sibling.m > min: # borrow from right
            node.add_pair(parent.p[parent_index].key, parent.p[parent_index].value, node.r) # add parent's separator key at last
            node.r = right_sibling.p[0].left_child # move right sibling's first left child to the node's rightmost child
            right_sibling.p[0].left_child.parent = node # its parent is node now
            
            # update parent's key
            parent.p[parent_index].key = right_sibling.p[0].key
            parent.p[parent_index].value = right_sibling.p[0].value
            
            right_sibling.remove_pair(0) # remove key from right sibling
        else: # merge
            if left_sibling: # with left
                self.merge_nonleaf(left_sibling, node, parent, parent_index - 1) # separator key is one before parent_index
            elif right_sibling: # with right
                self.merge_nonleaf(node, right_sibling, parent, parent_index)
    
    def merge_nonleaf(self, left, right, parent, separator): # merge nonleaf node with a sibling
        left.add_pair(parent.p[separator].key, parent.p[separator].value, left.r) # add separator key to left and set its left child as left's rightmost child
        left.p.extend(right.p) # merge with right
        left.m = len(left.p) # set m
        left.r = right.r # set r
        for i in range(left.m):
            left.p[i].left_child.parent = left # set all the left child's parent as left
        left.r.parent = left
        
        if separator + 1 < parent.m: # except two last index
            parent.p[separator+1].left_child = left # set left child as left
        elif separator + 1 == parent.m: # for second last index
            parent.r = left # set as rightmost child
        elif separator == parent.m: # for last index
            parent.r = left # set as rightmost child
        
        parent.remove_pair(separator) # remove separator from parent

        if parent.m < (self.degree - 1) // 2: # if parent is underflowed after deletion
            if parent == self.root: # if it is root, it's okay to be underflowed
                if parent.m == 0: # if empty
                    self.root = left # set left as new root
                    self.root.parent = None
            else:
                self.handle_internal_underflow(parent) # recursively handle nonleaf node's underflow
        
    def find_parent_index(self, node):
        if node.parent is not None:
            for i in range(node.parent.m):
                if node.parent.p[i].left_child == node: # found index where parent key has node as left child
                    return i
            if node.parent.r == node: # if rightmost child is node
                return node.parent.m # return the length of p
        return -1  # index does not exist
    
    def update_parent(self, node, new_key, new_value, old_key): # update nonleaf node's key with right child's smallest key value
        while True:
            for i in range(node.m):
                if old_key == node.p[i].key:
                    node.p[i].key = new_key
                    node.p[i].value = new_value
                    break
            
            if node == self.root: # stop when reach root
                return
            
            node = node.parent
            
    def update_parent_key(self, node): # update parent's key with its smallest key value
        if node.parent:
            parent = node.parent
            index = self.find_parent_index(node) # find the parent's index where node exists
            if index > 0: # update key value
                parent.p[index-1].key = node.p[0].key
                parent.p[index-1].value = node.p[0].value
                
    # Single Key Search
    def single_key_search(self, key):
        node = self.root
        key = int(key)
        
        if self.root is None: # tree is empty
            print(f"NOT FOUND")
            return None

        while not node.is_leaf: # until reach the leaf node
            print(",".join(str(pair.key) for pair in node.p)) # print all the pairs in path to key
            
            i = 0
            while i < node.m and key >= node.p[i].key:
                i += 1
            node = node.get_left_child(i) # move to its left child
        
        for pair in node.p: # in leaf node, print the value if found
            if pair.key == key:
                print(pair.value)
                return key
        print(f"NOT FOUND") # key not found
        return None
    
    # Ranged Search
    def ranged_search(self, start_key, end_key):
        node = self.root # start from root
        start_key = int(start_key)
        end_key = int(end_key)
        
        node = self.find_leaf_node(start_key) # find the node where start_key may exist
            
        while node is not None: # until reach the last leaf node
            for pair in node.p:
                if start_key <= pair.key <= end_key: # if the key is within the range, start printing
                    print(f"{pair.key},{pair.value}")
                elif pair.key > end_key: # if the key exceeds the end_key, stop searching
                    return
            node = node.r # move to right sibling

if __name__ == '__main__':
    if sys.argv[1] == '-c': # data file creation
        with open(sys.argv[2], 'w') as file:
            file.write(f"{sys.argv[3]}\n")
    elif sys.argv[1] == '-i': # insertion
        bptree = BPlusTree()
        bptree.load_tree(sys.argv[2])
        with open(sys.argv[3], 'r') as csv_file:
            reader = csv.reader(csv_file)
            for row in reader:
                key, value = int(row[0]), int(row[1])
                bptree.insertion(key, value)
        bptree.save_tree(sys.argv[2])
    elif sys.argv[1] == '-d': # deletion
        bptree = BPlusTree()
        bptree.load_tree(sys.argv[2])
        with open(sys.argv[3], 'r') as csv_file:
            reader=csv.reader(csv_file)
            for key in reader:
                bptree.deletion(int(key[0]))
        bptree.save_tree(sys.argv[2])
    elif sys.argv[1] == '-s': # single key search
        bptree = BPlusTree()
        bptree.load_tree(sys.argv[2])
        bptree.single_key_search(sys.argv[3])
    elif sys.argv[1] == '-r': # ranged search
        bptree = BPlusTree()
        bptree.load_tree(sys.argv[2])
        bptree.ranged_search(sys.argv[3], sys.argv[4])
    else:
        print(f"error in command line")