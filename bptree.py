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
            self.r = node
        else:
            self.p[index].left_child = node
    
    def get_left_child(self, index):
        if index == self.m:
            return self.r
        return self.p[index].left_child

class BPlusTree: # B+ Tree
    def __init__(self, degree=0):
        self.root = BPlusTreeNode()
        self.degree = degree # degree from input
    
    def save_tree(self, index_file):
        with open(index_file, 'w') as file:
            file.write(f"{self.degree}\n")
            self._save_node(self.root, file)

    def _save_node(self, node, index_file):
        if node is None or node.m == 0:
            return

        # node information (# of keys, is_leaf, and pairs)
        index_file.write(f"{node.m} {'1' if node.is_leaf else '0'} ")
        for pair in node.p:
            index_file.write(f"{pair.key},{pair.value} ")
        index_file.write("\n")

        # recursively save children for non-leaf nodes
        """if not node.is_leaf:
            child = node.get_left_child(0)
            while child:
                self._save_node(child, file)
                child = child.r"""
        if not node.is_leaf:
            for i in range(node.m + 1):
                child = node.get_left_child(i)
                if child:
                    self._save_node(child, index_file)
    
    def load(self, index_file):
        with open(index_file, 'r') as file:
            self.degree = int(file.readline().strip())
            nodes_data = file.readlines()
            
            if not nodes_data: # first initialization: when tree is empty
                self.root = BPlusTreeNode(is_leaf=True)
                return
            
            def build_node(data_line):
                node_info = data_line.strip().split()
                m = int(node_info[0])  # # of keys in a node
                is_leaf = bool(int(node_info[1]))  # 0 or 1 for is_leaf

                # create node
                node = BPlusTreeNode(is_leaf=is_leaf)
                node.m = m
                pairs = node_info[2:]  # key-value pairs
                for pair in pairs:
                    key, value = map(int, pair.split(','))
                    node.p.append(Pair(key, value))  # add key-value pair
                return node
            
            # recursive function to build tree
            def build_tree_from_data(index, last_leaf=None):
                if index >= len(nodes_data):
                    return None, index, last_leaf

                node = build_node(nodes_data[index])
                index += 1

                # if non-leaf, recursively build its children
                """if not node.is_leaf:
                    child = None
                    for i in range(node.m + 1):
                        child, index = build_tree_from_data(index)
                        if child:
                            child.parent = node
                        if i < len(node.p):
                            node.p[i].left_child = child
                            if child:
                                child.r = node.p[i + 1].left_child
                return node, index"""
                if not node.is_leaf:
                    for i in range(node.m + 1):
                        child, index, last_leaf = build_tree_from_data(index, last_leaf)
                        if child:
                            child.parent = node
                            node.set_left_child(i, child)
                    node.r = node.get_left_child(node.m) # rightmost child for non-leaf node
                
                if node.is_leaf: # right sibling node for leaf node
                    if last_leaf is not None:
                        last_leaf.r = node
                    last_leaf = node
                return node, index, last_leaf

            # building tree from the root
            self.root, _, _ = build_tree_from_data(0)

    # Insertion
    def insertion(self, key, value):
        leaf = self.find_leaf_node(key)
        
        # insert into leaf
        i = 0
        while i < leaf.m and key > leaf.p[i].key:
            i += 1
        
        if i < leaf.m and leaf.p[i].key == key: # debug for duplicated key
            print(f"Key {key} already exists")
            return 
        
        leaf.add_at_index(key, value, i)
        
        if leaf.m > self.degree - 1:
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

        new_leaf.r = leaf.r
        leaf.r = new_leaf
        
        self.propagate_split(leaf, new_leaf, new_leaf.p[0].key, new_leaf.p[0].value)

    def split_nonleaf_node(self, node):
        new_node = BPlusTreeNode(is_leaf=False)
        
        #middle = (node.m + 1) // 2
        middle = node.m // 2
        middle_key = node.p[middle].key
        middle_value = node.p[middle].value

        new_node.p = node.p[middle + 1:] # second half in new nonleaf node
        new_node.m = len(new_node.p)
        new_node.r = node.r
        
        temp = node.get_left_child(middle)

        node.p = node.p[:middle] # first half in original nonleaf node
        node.m = len(node.p)
        node.r = temp
        
        for pair in new_node.p: # update parent of new_node's children
            if pair.left_child:
                pair.left_child.parent = new_node
        if new_node.r:
            new_node.r.parent = new_node

        return middle_key, middle_value, new_node

    def propagate_split(self, old_node, new_node, new_key, new_value): # propagate the splitted key to parent node
        if old_node == self.root: # if it is root, create new root
            new_root = BPlusTreeNode(is_leaf=False)
            new_root.add_pair(new_key, new_value, old_node)
            #new_root.set_left_child(1, new_node)
            #new_root.set_left_child(0, old_node)
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
            parent.set_left_child(i, old_node)
            parent.set_left_child(i + 1, new_node)
            #parent.r = new_node
            
            new_node.parent = parent

            if parent.m > self.degree - 1: # if parent node overflows, split it
                middle_key, middle_value, new_parent = self.split_nonleaf_node(parent)
                self.propagate_split(parent, new_parent, middle_key, middle_value)
    
    # Deletion
    def deletion(self, key):
        leaf = self.find_leaf_node(key)
        print(f"key: {key}")
        
        i = 0
        while i < leaf.m:
            if leaf.p[i].key == key:
                break
            i += 1
        if i == leaf.m:
            print(f"{i}Key {key} not found in the tree")
            return

        self.delete_from_leaf(key, leaf, i)
    
    def delete_from_leaf(self, key, leaf, index):
        min = (self.degree - 1) // 2
        
        print(f"root key: {self.root.p[0].key} {self.root.m}")
        print(f"leaf index: {index} {leaf.m}")
        leaf.remove_pair(index)
        
        if 0 < leaf.m and key < leaf.p[0].key:
            self.update_parent(leaf, leaf.p[0].key, leaf.p[0].value, key)
        
        if leaf.m >= min:
            if index == 0 and leaf.parent:
                self.update_parent_key(leaf)
            return

        if leaf == self.root:
            if leaf.m == 0:
                self.root = None
            return
        
        parent = leaf.parent
        
        parent_index = self.find_parent_index(leaf)
        print(f"parent_index: {parent_index}")
        left_sibling = parent.get_left_child(parent_index - 1) if parent_index > 0 else None
        right_sibling = parent.get_left_child(parent_index + 1) if parent_index < parent.m else parent.r
        
        if left_sibling and left_sibling.m > min: # borrow from left
            leaf_first = leaf.p[0].key if leaf.m != 0 else key
            print(f"leaf-left {leaf_first}")
            leaf.add_at_index(left_sibling.p[-1].key, left_sibling.p[-1].value, 0)
            left_sibling.remove_pair(-1)
            
            self.update_parent_key(left_sibling)
            self.update_parent_key(leaf)
            """if parent_index-2 < parent.m and left_sibling.m != 0:
                parent.p[parent_index-2].key = left_sibling.p[0].key
                parent.p[parent_index-2].value = left_sibling.p[0].value
            
            if parent_index-1 < parent.m and leaf.m != 0:
                parent.p[parent_index-1].key = leaf.p[0].key
                parent.p[parent_index-1].value = leaf.p[0].value"""
            
            print(f"{leaf.p[0].key}")
            if leaf_first:
                self.update_parent(parent, leaf.p[0].key, leaf.p[0].value, leaf_first)
        elif right_sibling and right_sibling.m > min: # borrow from right
            print(f"leaf-right")
            leaf_first = leaf.p[0].key if leaf.m != 0 else key
            leaf.add_pair(right_sibling.p[0].key, right_sibling.p[0].value)
            right_sibling.remove_pair(0)
            print(f"{leaf_first}")
            
            self.update_parent_key(leaf)
            self.update_parent_key(right_sibling)
            """if parent_index-1 < parent.m and leaf.m != 0:
                parent.p[parent_index-1].key = leaf.p[0].key
                parent.p[parent_index-1].value = leaf.p[0].value
                print(f"1: {parent.p[parent_index-1].key}")
            
            if parent_index < parent.m and right_sibling.m != 0:
                parent.p[parent_index].key = right_sibling.p[0].key
                parent.p[parent_index].value = right_sibling.p[0].value
                print(f"2: {parent.p[parent_index].key}")"""
            
            if leaf_first:
                self.update_parent(parent, leaf.p[0].key, leaf.p[0].value, leaf_first)
        else:
            if left_sibling:
                self.merge_leaf(left_sibling, leaf, parent, parent_index - 1)
                print(f"leaf-merge1")
            elif right_sibling:
                self.merge_leaf(leaf, right_sibling, parent, parent_index)
                print(f"leaf-merge2")
                
        self.update_parent_key(leaf)
            
    def merge_leaf(self, left, right, parent, separator):
        right_first = right.p[0].key if right.m != 0 else None
        left.p.extend(right.p)
        left.m = len(left.p)
        left.r = right.r
        print(f"first: {left.p[0].key}")
        
        if right_first and parent.p[0].left_child == left:
            self.update_parent(parent, left.p[0].key, left.p[0].value, right_first)
        
        if separator + 1 < parent.m:
            parent.p[separator + 1].left_child = left
            if separator != 0:
                parent.p[separator].key = left.p[0].key
                parent.p[separator].value = left.p[0].value
            print(f"1")
            parent.remove_pair(separator)
        elif separator + 1 == parent.m:
            parent.r = left
            parent.remove_pair(separator)
            if parent.m != 0:
                parent.p[-1].key = left.p[0].key
                parent.p[-1].value = left.p[0].value
            print(f"2")
        elif separator == parent.m:
            parent.r = left
            parent.remove_pair(separator)
            if parent.m != 0:
                parent.p[-1].key = left.p[0].key
                parent.p[-1].value = left.p[0].value
            print(f"3")

        if parent.m < (self.degree - 1) // 2: # when underflow
            if parent == self.root: # except root
                if parent.m == 0:
                    self.root = left
                    self.root.parent = None
            else:
                self.handle_internal_underflow(parent) # handle underflow
                
        self.update_parent_key(left) #????? is it needed..?
    
    def merge_nonleaf_left(self, left, right, parent, separator): # merge nonleaf node with left sibling
        #right.r == merged_node
        left.p.extend(right.p)
        left.m = len(left.p)
         
        left.add_pair(parent.p[separator].key, parent.p[separator].value, left.r)
        left.r = right.r
        for i in range(left.m):
            left.p[i].left_child.parent = left
        left.r.parent = left
        if separator + 1 < parent.m:
            parent.p[separator+1].left_child = left
        elif separator + 1 == parent.m:
            parent.r = left
        elif separator == parent.m:
            parent.r = left
            
        parent.remove_pair(separator)
        
        if parent.m < (self.degree - 1) // 2:
            if parent == self.root:
                if parent.m == 0:
                    self.root = left
                    self.root.parent = None
            else:
                self.handle_internal_underflow(parent)        
         
    def merge_nonleaf_right(self, left, right, parent, separator): # merge nonleaf node with right sibling
        #left.r == merged_node
        left.p.extend(right.p)
        left.m = len(left.p)
        
        left.add_at_index(parent.p[separator].key, parent.p[separator].value, 0)
        left.p[0].left_child = left.r
        print(f"left.r: {left.p[0].left_child.p[0].key}")
        left.r = right.r
        for i in range(left.m):
            left.p[i].left_child.parent = left
        left.r.parent = left
        if separator + 1 < parent.m:
            parent.p[separator+1].left_child = left
        elif separator + 1 == parent.m:
            parent.r = left
        elif separator == parent.m:
            parent.r = left
        
        parent.remove_pair(separator)

        if parent.m < (self.degree - 1) // 2:
            if parent == self.root:
                if parent.m == 0:
                    self.root = left
                    self.root.parent = None
            else:
                self.handle_internal_underflow(parent)
        
    def handle_internal_underflow(self, node):
        min = (self.degree - 1) // 2
        
        parent = node.parent
        parent_index = self.find_parent_index(node)
        print(f"par_index: {parent_index}")
        left_sibling = parent.get_left_child(parent_index - 1) if parent_index > 0 else None
        right_sibling = parent.get_left_child(parent_index + 1) if parent_index < parent.m else parent.r
        
        if left_sibling and left_sibling.m > min: # borrow from left
            print(f"non-left")
            node.add_at_index(parent.p[parent_index - 1].key, parent.p[parent_index - 1].value, 0)
            node.p[0].left_child = left_sibling.r
            node.p[0].left_child.parent = node
            left_sibling.r = left_sibling.p[-1].left_child
            
            parent.p[parent_index-1].key = left_sibling.p[-1].key
            parent.p[parent_index-1].value = left_sibling.p[-1].value
            
            left_sibling.remove_pair(-1)
            
            #parent.p[parent_index-1].key = node.p[0].key
            #parent.p[parent_index-1].value = node.p[0].value
        elif right_sibling and right_sibling.m > min: # borrow from right
            print(f"non-right")
            node.add_pair(parent.p[parent_index].key, parent.p[parent_index].value, node.r)
            node.r = right_sibling.p[0].left_child
            right_sibling.p[0].left_child.parent = node
            
            parent.p[parent_index].key = right_sibling.p[0].key
            parent.p[parent_index].value = right_sibling.p[0].value
            
            right_sibling.remove_pair(0)
            
            #parent.p[parent_index].key = right_sibling.p[0].key
            #parent.p[parent_index].value = right_sibling.p[0].value
        else:
            # Merge with a sibling
            if left_sibling:
                print(f"merge-left")
                self.merge_nonleaf_left(left_sibling, node, parent, parent_index - 1)
            elif right_sibling:
                print(f"merge-right")
                self.merge_nonleaf_right(node, right_sibling, parent, parent_index)
    
    def find_parent_index(self, node):
        if node.parent is not None:
            for i in range(node.parent.m):
                if node.parent.p[i].left_child == node:
                    return i
            if node.parent.r == node:
                print(f"findindex-2: {node.parent.m}")
                return node.parent.m
        return -1  # index does not exist
    
    def update_parent(self, node, new_key, new_value, old_key): # update nonleaf node's key with right child's smallest key value
        while True:
            for i in range(node.m):
                if old_key == node.p[i].key:
                    node.p[i].key = new_key
                    node.p[i].value = new_value
                    break
            
            if node == self.root:
                return
            
            node = node.parent
            
    def update_parent_key(self, node): # update parent's key with its smallest key value
        if node.parent: #and not node.is_leaf
            parent = node.parent
            index = self.find_parent_index(node)
            print(f"index {index}")
            if index > 0: #index < parent.m and 
                parent.p[index-1].key = node.p[0].key
                parent.p[index-1].value = node.p[0].value
                
    # Single Key Search
    def single_key_search(self, key):
        node = self.root
        key = int(key)
        
        if self.root is None:
            print(f"NOT FOUND")

        while not node.is_leaf:
            print(",".join(str(pair.key) for pair in node.p))
            
            i = 0
            while i < node.m and key >= node.p[i].key:
                i += 1
            node = node.get_left_child(i)
        
        for pair in node.p: # in leaf node, print the value if found
            if pair.key == key:
                print(pair.value)
                return
        print(f"NOT FOUND")
    
    # Ranged Search
    def ranged_search(self, start_key, end_key):
        node = self.root
        start_key = int(start_key)
        end_key = int(end_key)
        
        node = self.find_leaf_node(start_key)
        
        #search_started = False
            
        while node is not None:
            for pair in node.p:
                # if the key is within the range, start printing
                if start_key <= pair.key <= end_key:
                    print(f"{pair.key},{pair.value}")
                    #search_started = True
                # if the key exceeds the end_key, stop searching
                elif pair.key > end_key:
                    return
            node = node.r

if __name__ == '__main__':
    if sys.argv[1] == '-c':
        with open(sys.argv[2], 'w') as file:
            file.write(f"{sys.argv[3]}\n")
    elif sys.argv[1] == '-i':
        bptree = BPlusTree()
        bptree.load(sys.argv[2])
        with open(sys.argv[3], 'r') as csv_file:
            reader = csv.reader(csv_file)
            for row in reader:
                key, value = int(row[0]), int(row[1])
                bptree.insertion(key, value)
        bptree.save_tree(sys.argv[2])
    elif sys.argv[1] == '-s':
        bptree = BPlusTree()
        bptree.load(sys.argv[2])
        bptree.single_key_search(sys.argv[3])
    elif sys.argv[1] == '-r':
        bptree = BPlusTree()
        bptree.load(sys.argv[2])
        bptree.ranged_search(sys.argv[3], sys.argv[4])
    elif sys.argv[1] == '-d':
        bptree = BPlusTree()
        bptree.load(sys.argv[2])
        with open(sys.argv[3], 'r') as csv_file:
            reader=csv.reader(csv_file)
            for key in reader:
                bptree.deletion(int(key[0]))
        bptree.save_tree(sys.argv[2])
    else:
        print(f"error in command line")