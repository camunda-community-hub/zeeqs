// Copyright © 2020 Camunda Services GmbH (info@camunda.com)
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package commands

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"github.com/spf13/cobra"
)

var workflowsCmd = &cobra.Command{
	Use:   "workflows",
	Short: "Query deployed workflows",
	Args:  cobra.NoArgs,
	RunE: func(cmd *cobra.Command, args []string) error {
		return queryWorkflows()
	},
}

func queryWorkflows() error {

	requestBody, err := json.Marshal(map[string]string{
		"query": "{ workflows { nodes { key } } }",
	})

	if (err != nil) {
		return err
	}

	resp, err := http.Post(
		"http://localhost:9000/graphql",
		"application/json",
		bytes.NewBuffer(requestBody),
	)

	if (err != nil) {
		return err
	}

	defer resp.Body.Close()

	body, err := ioutil.ReadAll(resp.Body)

	if (err != nil) {
		return err
	}

	fmt.Println(string(body))
	return nil
}

func init() {
	rootCmd.AddCommand(workflowsCmd)
}
